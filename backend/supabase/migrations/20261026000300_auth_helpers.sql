-- =====================================================================
-- EduPonto — migração 3: apoio ao login
--
-- mark_password_changed(): o usuário chama depois de trocar a senha provisória.
-- Observação: isto é uma trava de uso (o app exige a troca no primeiro acesso),
-- não uma barreira de segurança: o servidor não consegue provar que a senha mudou.
-- =====================================================================
create function public.mark_password_changed()
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
  update public.users
     set must_change_password = false
   where id = auth.uid();
end;
$$;

revoke all on function public.mark_password_changed() from public, anon, authenticated;
grant execute on function public.mark_password_changed() to authenticated;
