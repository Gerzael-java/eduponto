-- =====================================================================
-- EduPonto — migração 2: segurança (RLS), privilégios e auditoria
--
-- Princípios:
--  1. O cliente (app) nunca é confiável: a autorização é feita aqui, no banco.
--  2. Ninguém sem login (anon) acessa nada.
--  3. Tabelas críticas (horários, ocorrências, chamadas, presenças, correções)
--     são SOMENTE LEITURA para o app. Toda alteração passa por funções do
--     servidor (RPC), criadas nas próximas migrações, que validam tudo.
--  4. Hashes de token/código da chamada nunca são legíveis pelo app.
--  5. Alterações relevantes geram registros de auditoria imutáveis.
-- =====================================================================

-- ---------- funções auxiliares de autorização ----------
create function public.has_role(_role text)
returns boolean
language sql stable security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.user_roles ur
    join public.users u on u.id = ur.user_id
    where ur.user_id = auth.uid()
      and ur.role = _role
      and u.status = 'active'
  );
$$;

create function public.is_admin()
returns boolean language sql stable security definer set search_path = ''
as $$ select public.has_role('admin'); $$;

create function public.is_teacher()
returns boolean language sql stable security definer set search_path = ''
as $$ select public.has_role('teacher'); $$;

create function public.is_student()
returns boolean language sql stable security definer set search_path = ''
as $$ select public.has_role('student'); $$;

create function public.teaches_class(_class_id uuid)
returns boolean
language sql stable security definer
set search_path = ''
as $$
  select exists (
    select 1 from public.class_teachers ct
    where ct.class_id = _class_id
      and ct.teacher_id = auth.uid()
      and ct.ended_at is null
  );
$$;

create function public.is_enrolled(_class_id uuid)
returns boolean
language sql stable security definer
set search_path = ''
as $$
  select exists (
    select 1 from public.enrollments e
    where e.class_id = _class_id
      and e.student_id = auth.uid()
      and e.status <> 'ended'
  );
$$;

-- Professor e aluno que compartilham uma turma enxergam o nome um do outro.
create function public.shares_class_with(_other uuid)
returns boolean
language sql stable security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.enrollments e
    join public.class_teachers ct on ct.class_id = e.class_id and ct.ended_at is null
    where e.status <> 'ended'
      and (
        (ct.teacher_id = auth.uid() and e.student_id = _other)
        or (e.student_id = auth.uid() and ct.teacher_id = _other)
      )
  );
$$;

-- Quem gerencia a chamada: administrador, o professor da chamada ou professor da turma.
create function public.can_manage_attendance_session(_id uuid)
returns boolean
language sql stable security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.attendance_sessions a
    join public.class_sessions cs on cs.id = a.class_session_id
    join public.schedules s on s.id = cs.schedule_id
    where a.id = _id
      and (
        public.is_admin()
        or a.teacher_id = auth.uid()
        or public.teaches_class(s.class_id)
      )
  );
$$;

-- Quem pode VER que existe uma chamada: quem a gerencia e os alunos matriculados.
create function public.can_see_attendance_session(_id uuid)
returns boolean
language sql stable security definer
set search_path = ''
as $$
  select public.can_manage_attendance_session(_id)
    or exists (
      select 1
      from public.attendance_sessions a
      join public.class_sessions cs on cs.id = a.class_session_id
      join public.schedules s on s.id = cs.schedule_id
      where a.id = _id and public.is_enrolled(s.class_id)
    );
$$;

-- ---------- privilégios ----------
revoke all on all tables in schema public from anon, authenticated;
revoke all on all functions in schema public from public, anon, authenticated;

alter default privileges for role postgres in schema public
  revoke all on tables from anon, authenticated;
alter default privileges for role postgres in schema public
  revoke all on functions from public, anon, authenticated;

-- leitura (as políticas RLS abaixo filtram as linhas)
grant select on
  public.institution_settings, public.users, public.user_roles, public.classes,
  public.class_teachers, public.enrollments, public.subjects, public.attendance_policies,
  public.schedules, public.schedule_weekdays, public.schedule_exceptions,
  public.class_sessions, public.attendance_records, public.correction_requests,
  public.audit_logs, public.notifications
  to authenticated;

-- chamadas: tudo, exceto os hashes
grant select (id, class_session_id, teacher_id, status, opened_at, closed_at,
              code_validity_seconds, token_version, token_expires_at, created_at)
  on public.attendance_sessions to authenticated;

-- escrita direta (limitada pelas políticas): estrutura administrativa
grant update on public.institution_settings, public.users to authenticated;
grant insert, update on
  public.classes, public.class_teachers, public.enrollments, public.attendance_policies
  to authenticated;
grant insert, update on public.subjects to authenticated;
grant insert, update, delete on public.user_roles to authenticated;
grant update (status, read_at) on public.notifications to authenticated;

grant execute on function
  public.has_role(text), public.is_admin(), public.is_teacher(), public.is_student(),
  public.teaches_class(uuid), public.is_enrolled(uuid), public.shares_class_with(uuid),
  public.can_manage_attendance_session(uuid), public.can_see_attendance_session(uuid)
  to authenticated;

-- ---------- RLS ligada em todas as tabelas ----------
do $$
declare
  t record;
begin
  for t in select tablename from pg_tables where schemaname = 'public' loop
    execute format('alter table public.%I enable row level security', t.tablename);
  end loop;
end;
$$;

-- ---------- políticas ----------
-- configurações da instituição
create policy institution_select on public.institution_settings
  for select to authenticated using (true);
create policy institution_update on public.institution_settings
  for update to authenticated using (public.is_admin()) with check (public.is_admin());

-- usuários (criados por função de servidor; o app não insere)
create policy users_select on public.users
  for select to authenticated
  using (id = auth.uid() or public.is_admin() or public.shares_class_with(id));
create policy users_update_admin on public.users
  for update to authenticated using (public.is_admin()) with check (public.is_admin());

-- papéis
create policy user_roles_select on public.user_roles
  for select to authenticated using (user_id = auth.uid() or public.is_admin());
create policy user_roles_insert on public.user_roles
  for insert to authenticated with check (public.is_admin());
create policy user_roles_update on public.user_roles
  for update to authenticated using (public.is_admin()) with check (public.is_admin());
create policy user_roles_delete on public.user_roles
  for delete to authenticated using (public.is_admin());

-- turmas
create policy classes_select on public.classes
  for select to authenticated
  using (public.is_admin() or public.teaches_class(id) or public.is_enrolled(id));
create policy classes_insert on public.classes
  for insert to authenticated with check (public.is_admin());
create policy classes_update on public.classes
  for update to authenticated using (public.is_admin()) with check (public.is_admin());

-- professores da turma
create policy class_teachers_select on public.class_teachers
  for select to authenticated
  using (public.is_admin() or teacher_id = auth.uid() or public.is_enrolled(class_id));
create policy class_teachers_insert on public.class_teachers
  for insert to authenticated with check (public.is_admin());
create policy class_teachers_update on public.class_teachers
  for update to authenticated using (public.is_admin()) with check (public.is_admin());

-- matrículas
create policy enrollments_select on public.enrollments
  for select to authenticated
  using (public.is_admin() or student_id = auth.uid() or public.teaches_class(class_id));
create policy enrollments_insert on public.enrollments
  for insert to authenticated with check (public.is_admin());
create policy enrollments_update on public.enrollments
  for update to authenticated using (public.is_admin()) with check (public.is_admin());

-- disciplinas: todos leem; administrador e professores cadastram; só o administrador altera
create policy subjects_select on public.subjects
  for select to authenticated using (true);
create policy subjects_insert on public.subjects
  for insert to authenticated
  with check (public.is_admin() or (public.is_teacher() and created_by = auth.uid()));
create policy subjects_update on public.subjects
  for update to authenticated using (public.is_admin()) with check (public.is_admin());

-- regras de presença
create policy policies_select on public.attendance_policies
  for select to authenticated using (true);
create policy policies_insert on public.attendance_policies
  for insert to authenticated with check (public.is_admin());
create policy policies_update on public.attendance_policies
  for update to authenticated using (public.is_admin()) with check (public.is_admin());

-- horários e ocorrências: somente leitura (escrita via RPC)
create policy schedules_select on public.schedules
  for select to authenticated
  using (public.is_admin() or teacher_id = auth.uid()
         or public.teaches_class(class_id) or public.is_enrolled(class_id));
create policy schedule_weekdays_select on public.schedule_weekdays
  for select to authenticated
  using (exists (select 1 from public.schedules s where s.id = schedule_id));
create policy schedule_exceptions_select on public.schedule_exceptions
  for select to authenticated
  using (exists (select 1 from public.schedules s where s.id = schedule_id));
create policy class_sessions_select on public.class_sessions
  for select to authenticated
  using (exists (select 1 from public.schedules s where s.id = schedule_id));

-- chamadas e presenças: somente leitura (escrita via RPC)
create policy attendance_sessions_select on public.attendance_sessions
  for select to authenticated using (public.can_see_attendance_session(id));
create policy attendance_records_select on public.attendance_records
  for select to authenticated
  using (student_id = auth.uid() or public.can_manage_attendance_session(attendance_session_id));
create policy correction_requests_select on public.correction_requests
  for select to authenticated
  using (requester_id = auth.uid()
         or public.can_manage_attendance_session(attendance_session_id));

-- attendance_attempts: sem política = ninguém acessa pelo app

-- auditoria: só o administrador lê
create policy audit_logs_select on public.audit_logs
  for select to authenticated using (public.is_admin());

-- notificações: cada um lê e marca como lidas as suas
create policy notifications_select on public.notifications
  for select to authenticated using (recipient_id = auth.uid());
create policy notifications_update on public.notifications
  for update to authenticated
  using (recipient_id = auth.uid()) with check (recipient_id = auth.uid());

-- ---------- proteção contra ficar sem administrador ----------
create function public.protect_last_admin_role()
returns trigger
language plpgsql security definer
set search_path = ''
as $$
begin
  if old.role = 'admin' and (tg_op = 'DELETE' or new.role <> 'admin') then
    if not exists (
      select 1
      from public.user_roles ur
      join public.users u on u.id = ur.user_id
      where ur.role = 'admin' and u.status = 'active' and ur.id <> old.id
    ) then
      raise exception 'Não é possível remover o último administrador ativo.';
    end if;
  end if;
  if tg_op = 'DELETE' then
    return old;
  end if;
  return new;
end;
$$;

create trigger user_roles_protect_last_admin
  before update or delete on public.user_roles
  for each row execute function public.protect_last_admin_role();

create function public.protect_last_admin_user()
returns trigger
language plpgsql security definer
set search_path = ''
as $$
begin
  if old.status = 'active' and new.status = 'inactive'
     and exists (select 1 from public.user_roles ur
                 where ur.user_id = old.id and ur.role = 'admin') then
    if not exists (
      select 1
      from public.user_roles ur
      join public.users u on u.id = ur.user_id
      where ur.role = 'admin' and u.status = 'active' and u.id <> old.id
    ) then
      raise exception 'Não é possível desativar o último administrador ativo.';
    end if;
  end if;
  return new;
end;
$$;

create trigger users_protect_last_admin
  before update on public.users
  for each row execute function public.protect_last_admin_user();

-- ---------- auditoria ----------
-- Remove dados pessoais e segredos antes de registrar.
create function public.audit_row_change()
returns trigger
language plpgsql security definer
set search_path = ''
as $$
declare
  v_strip  text[] := array['email', 'registration_code', 'token_hash', 'alt_code_hash'];
  v_old    jsonb;
  v_new    jsonb;
  v_actor  uuid;
begin
  if tg_op in ('UPDATE', 'DELETE') then
    v_old := to_jsonb(old) - v_strip;
  end if;
  if tg_op in ('INSERT', 'UPDATE') then
    v_new := to_jsonb(new) - v_strip;
  end if;

  select u.id into v_actor from public.users u where u.id = auth.uid();

  insert into public.audit_logs (actor_id, action, entity, entity_id, old_value, new_value)
  values (
    v_actor,
    lower(tg_op),
    tg_table_name,
    coalesce(v_new ->> 'id', v_old ->> 'id'),
    v_old,
    v_new
  );
  return null;
end;
$$;

do $$
declare
  t text;
begin
  foreach t in array array[
    'institution_settings', 'users', 'user_roles', 'classes', 'class_teachers',
    'enrollments', 'subjects', 'attendance_policies', 'schedules', 'schedule_exceptions',
    'attendance_sessions', 'attendance_records', 'correction_requests'
  ] loop
    execute format(
      'create trigger %I after insert or update or delete on public.%I
         for each row execute function public.audit_row_change()',
      t || '_audit', t
    );
  end loop;
end;
$$;

-- o registro de auditoria não pode ser alterado nem apagado
create function public.audit_logs_immutable()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  raise exception 'audit_logs é somente de inserção.';
end;
$$;

create trigger audit_logs_no_change
  before update or delete on public.audit_logs
  for each row execute function public.audit_logs_immutable();

create trigger audit_logs_no_truncate
  before truncate on public.audit_logs
  for each statement execute function public.audit_logs_immutable();
