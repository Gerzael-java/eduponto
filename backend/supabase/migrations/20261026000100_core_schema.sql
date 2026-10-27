-- =====================================================================
-- EduPonto — migração 1: esquema principal
-- Instituição única. Identificadores uuid. Instantes em timestamptz (UTC).
-- Valores de domínio usam text + CHECK (mais fácil de evoluir que enum).
-- Chaves estrangeiras com ON DELETE RESTRICT: histórico nunca é apagado
-- por acidente; usuários, turmas e matrículas são desativados/encerrados.
-- =====================================================================

-- ---------- configurações da instituição (uma única linha) ----------
create table public.institution_settings (
  id          boolean primary key default true,
  name        text not null
              check (char_length(btrim(name)) between 2 and 160),
  timezone    text not null default 'America/Sao_Paulo',
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  constraint institution_settings_single_row check (id)
);

-- ---------- usuários (perfil; a identidade e a senha ficam em auth.users) ----------
create table public.users (
  id                    uuid primary key references auth.users (id) on delete restrict,
  full_name             text not null
                        check (char_length(btrim(full_name)) between 2 and 120),
  email                 text not null,
  registration_code     text check (char_length(registration_code) <= 40),
  status                text not null default 'active'
                        check (status in ('active', 'inactive')),
  must_change_password  boolean not null default true,
  created_at            timestamptz not null default now(),
  updated_at            timestamptz not null default now()
);
create unique index users_email_uq on public.users (lower(email));
create unique index users_registration_code_uq
  on public.users (registration_code) where registration_code is not null;
create index users_status_idx on public.users (status);

-- ---------- papéis ----------
create table public.user_roles (
  id          uuid primary key default gen_random_uuid(),
  user_id     uuid not null references public.users (id) on delete restrict,
  role        text not null check (role in ('admin', 'teacher', 'student')),
  granted_by  uuid references public.users (id) on delete restrict,
  created_at  timestamptz not null default now(),
  unique (user_id, role)
);
create index user_roles_role_idx on public.user_roles (role);

-- ---------- turmas ----------
create table public.classes (
  id           uuid primary key default gen_random_uuid(),
  name         text not null check (char_length(btrim(name)) between 1 and 80),
  description  text check (char_length(description) <= 500),
  status       text not null default 'active' check (status in ('active', 'archived')),
  created_by   uuid references public.users (id) on delete restrict,
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now()
);
create unique index classes_active_name_uq
  on public.classes (lower(btrim(name))) where status = 'active';

-- ---------- professores vinculados às turmas ----------
create table public.class_teachers (
  id          uuid primary key default gen_random_uuid(),
  class_id    uuid not null references public.classes (id) on delete restrict,
  teacher_id  uuid not null references public.users (id) on delete restrict,
  created_at  timestamptz not null default now(),
  ended_at    timestamptz,
  check (ended_at is null or ended_at >= created_at)
);
create unique index class_teachers_active_uq
  on public.class_teachers (class_id, teacher_id) where ended_at is null;
create index class_teachers_teacher_idx
  on public.class_teachers (teacher_id) where ended_at is null;

-- ---------- matrículas ----------
create table public.enrollments (
  id           uuid primary key default gen_random_uuid(),
  student_id   uuid not null references public.users (id) on delete restrict,
  class_id     uuid not null references public.classes (id) on delete restrict,
  enrolled_at  date not null default current_date,
  status       text not null default 'active'
               check (status in ('active', 'suspended', 'ended')),
  ended_at     date,
  created_at   timestamptz not null default now(),
  check (ended_at is null or ended_at >= enrolled_at),
  check ((status = 'ended') = (ended_at is not null))
);
-- Impede matrículas duplicadas (ativa ou suspensa) do mesmo aluno na mesma turma.
create unique index enrollments_open_uq
  on public.enrollments (student_id, class_id) where status <> 'ended';
create index enrollments_class_idx on public.enrollments (class_id) where status <> 'ended';
create index enrollments_student_idx on public.enrollments (student_id);

-- ---------- disciplinas ou atividades (nomes totalmente dinâmicos) ----------
create table public.subjects (
  id           uuid primary key default gen_random_uuid(),
  name         text not null check (char_length(btrim(name)) between 1 and 80),
  description  text check (char_length(description) <= 500),
  status       text not null default 'active' check (status in ('active', 'inactive')),
  created_by   uuid references public.users (id) on delete restrict,
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now()
);
create unique index subjects_active_name_uq
  on public.subjects (lower(btrim(name))) where status = 'active';

-- ---------- regras de presença (tolerância, validade do código...) ----------
create table public.attendance_policies (
  id                        uuid primary key default gen_random_uuid(),
  name                      text not null check (char_length(btrim(name)) between 1 and 80),
  late_after_minutes        integer not null default 10 check (late_after_minutes >= 0),
  code_validity_seconds     integer not null default 1200
                            check (code_validity_seconds between 30 and 86400),
  allow_code_renewal        boolean not null default true,
  allow_manual_registration boolean not null default true,
  is_default                boolean not null default false,
  created_at                timestamptz not null default now(),
  updated_at                timestamptz not null default now()
);
create unique index attendance_policies_default_uq
  on public.attendance_policies (is_default) where is_default;

-- ---------- horários (regras planejadas e recorrência) ----------
create table public.schedules (
  id                uuid primary key default gen_random_uuid(),
  class_id          uuid not null references public.classes (id) on delete restrict,
  subject_id        uuid not null references public.subjects (id) on delete restrict,
  teacher_id        uuid not null references public.users (id) on delete restrict,
  policy_id         uuid references public.attendance_policies (id) on delete restrict,
  start_time        time not null,
  end_time          time not null,
  crosses_midnight  boolean not null default false,
  recurrence        text not null check (recurrence in ('single', 'weekly')),
  valid_from        date not null,
  valid_until       date,
  location          text check (char_length(location) <= 120),
  status            text not null default 'active'
                    check (status in ('active', 'inactive', 'cancelled')),
  created_by        uuid references public.users (id) on delete restrict,
  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now(),
  check (crosses_midnight or end_time > start_time),
  check (valid_until is null or valid_until >= valid_from),
  -- aula única: acontece em uma data só
  check (recurrence <> 'single' or (valid_until is not null and valid_until = valid_from))
);
create index schedules_teacher_idx on public.schedules (teacher_id, status);
create index schedules_class_idx on public.schedules (class_id, status);
create index schedules_subject_idx on public.schedules (subject_id);

-- dias da semana de um horário semanal (1 = segunda ... 7 = domingo)
create table public.schedule_weekdays (
  schedule_id  uuid not null references public.schedules (id) on delete cascade,
  weekday      smallint not null check (weekday between 1 and 7),
  primary key (schedule_id, weekday)
);

-- exceções em datas específicas (cancelamento ou remarcação)
create table public.schedule_exceptions (
  id               uuid primary key default gen_random_uuid(),
  schedule_id      uuid not null references public.schedules (id) on delete restrict,
  exception_date   date not null,
  kind             text not null check (kind in ('cancelled', 'rescheduled')),
  new_start_time   time,
  new_end_time     time,
  reason           text check (char_length(reason) <= 300),
  created_by       uuid references public.users (id) on delete restrict,
  created_at       timestamptz not null default now(),
  unique (schedule_id, exception_date),
  check (kind = 'cancelled' or (new_start_time is not null and new_end_time is not null))
);

-- ---------- ocorrências reais de aula ----------
create table public.class_sessions (
  id             uuid primary key default gen_random_uuid(),
  schedule_id    uuid not null references public.schedules (id) on delete restrict,
  session_date   date not null,
  planned_start  timestamptz not null,
  planned_end    timestamptz not null,
  actual_start   timestamptz,
  actual_end     timestamptz,
  status         text not null default 'planned'
                 check (status in ('planned', 'in_progress', 'completed', 'cancelled')),
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now(),
  unique (schedule_id, session_date),
  check (planned_end > planned_start),
  check (actual_end is null or actual_start is null or actual_end >= actual_start)
);
create index class_sessions_date_idx on public.class_sessions (session_date);

-- ---------- chamadas ----------
-- O token e o código alternativo NUNCA são guardados em claro: só o hash.
create table public.attendance_sessions (
  id                     uuid primary key default gen_random_uuid(),
  class_session_id       uuid not null references public.class_sessions (id) on delete restrict,
  teacher_id             uuid not null references public.users (id) on delete restrict,
  status                 text not null default 'open'
                         check (status in ('open', 'closed', 'cancelled')),
  opened_at              timestamptz not null default now(),
  closed_at              timestamptz,
  code_validity_seconds  integer not null check (code_validity_seconds > 0),
  token_hash             text not null,
  token_version          integer not null default 1,
  token_expires_at       timestamptz not null,
  alt_code_hash          text not null,
  created_at             timestamptz not null default now(),
  check (closed_at is null or closed_at >= opened_at),
  check ((status = 'open') = (closed_at is null))
);
-- no máximo uma chamada aberta por ocorrência de aula
create unique index attendance_sessions_one_open_uq
  on public.attendance_sessions (class_session_id) where status = 'open';
create index attendance_sessions_teacher_idx
  on public.attendance_sessions (teacher_id, status);

-- ---------- registros de presença ----------
create table public.attendance_records (
  id                     uuid primary key default gen_random_uuid(),
  attendance_session_id  uuid not null references public.attendance_sessions (id) on delete restrict,
  student_id             uuid not null references public.users (id) on delete restrict,
  status                 text not null
                         check (status in ('present', 'late', 'absent', 'justified')),
  recorded_at            timestamptz not null default now(),   -- hora do servidor
  origin                 text not null check (origin in ('qr', 'code', 'manual')),
  client_request_id      uuid not null,                        -- idempotência
  recorded_by            uuid references public.users (id) on delete restrict,
  is_active              boolean not null default true,
  created_at             timestamptz not null default now(),
  updated_at             timestamptz not null default now()
);
-- um aluno só pode ter um registro ativo por chamada
create unique index attendance_records_active_uq
  on public.attendance_records (attendance_session_id, student_id) where is_active;
-- a mesma solicitação repetida não cria outro registro
create unique index attendance_records_request_uq
  on public.attendance_records (client_request_id);
create index attendance_records_student_idx
  on public.attendance_records (student_id, recorded_at desc);

-- ---------- tentativas de registro (para limitar tentativas automatizadas) ----------
create table public.attendance_attempts (
  id                     bigint generated always as identity primary key,
  attendance_session_id  uuid not null references public.attendance_sessions (id) on delete restrict,
  student_id             uuid not null references public.users (id) on delete restrict,
  success                boolean not null,
  created_at             timestamptz not null default now()
);
create index attendance_attempts_recent_idx
  on public.attendance_attempts (attendance_session_id, student_id, created_at desc);

-- ---------- solicitações de correção ----------
create table public.correction_requests (
  id                     uuid primary key default gen_random_uuid(),
  attendance_session_id  uuid not null references public.attendance_sessions (id) on delete restrict,
  record_id              uuid references public.attendance_records (id) on delete restrict,
  requester_id           uuid not null references public.users (id) on delete restrict,
  reason_type            text not null
                         check (reason_type in ('technical_problem', 'late_registration',
                                                'wrong_status', 'other')),
  description            text not null check (char_length(btrim(description)) between 10 and 1000),
  requested_status       text not null
                         check (requested_status in ('present', 'late', 'absent', 'justified')),
  status                 text not null default 'pending'
                         check (status in ('pending', 'needs_info', 'approved', 'rejected')),
  reviewer_id            uuid references public.users (id) on delete restrict,
  decision_note          text check (char_length(decision_note) <= 1000),
  created_at             timestamptz not null default now(),
  decided_at             timestamptz,
  check ((status in ('approved', 'rejected')) = (decided_at is not null))
);
create index correction_requests_requester_idx on public.correction_requests (requester_id, status);
create index correction_requests_session_idx on public.correction_requests (attendance_session_id);

-- ---------- auditoria (somente inserção; ver migração 2) ----------
create table public.audit_logs (
  id          bigint generated always as identity primary key,
  actor_id    uuid references public.users (id) on delete restrict,
  action      text not null,
  entity      text not null,
  entity_id   text not null,
  old_value   jsonb,
  new_value   jsonb,
  reason      text,
  created_at  timestamptz not null default now()
);
create index audit_logs_entity_idx on public.audit_logs (entity, entity_id);
create index audit_logs_created_idx on public.audit_logs (created_at desc);

-- ---------- notificações ----------
create table public.notifications (
  id            uuid primary key default gen_random_uuid(),
  recipient_id  uuid not null references public.users (id) on delete restrict,
  type          text not null,
  payload       jsonb not null default '{}'::jsonb,
  status        text not null default 'pending' check (status in ('pending', 'sent', 'read')),
  created_at    timestamptz not null default now(),
  read_at       timestamptz
);
create index notifications_recipient_idx on public.notifications (recipient_id, status);

-- ---------- atualização automática de updated_at ----------
create function public.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

do $$
declare
  t text;
begin
  foreach t in array array[
    'institution_settings', 'users', 'classes', 'subjects', 'attendance_policies',
    'schedules', 'class_sessions', 'attendance_records'
  ] loop
    execute format(
      'create trigger %I before update on public.%I
         for each row execute function public.set_updated_at()',
      t || '_set_updated_at', t
    );
  end loop;
end;
$$;
