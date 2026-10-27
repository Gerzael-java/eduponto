-- =====================================================================
-- EduPonto — verificação da configuração de segurança
-- Rode no SQL Editor do Supabase DEPOIS das migrações e me envie o resultado.
-- =====================================================================

-- 1) Tabelas SEM RLS (esperado: nenhuma linha)
select tablename as tabela_sem_rls
from pg_tables
where schemaname = 'public' and not rowsecurity;

-- 2) Privilégios do papel "anon" (usuário sem login) (esperado: nenhuma linha)
select table_name, privilege_type
from information_schema.role_table_grants
where grantee = 'anon' and table_schema = 'public';

-- 3) Colunas de attendance_sessions que o app pode ler
--    (esperado: NÃO aparecer token_hash nem alt_code_hash)
select column_name
from information_schema.column_privileges
where grantee = 'authenticated'
  and table_schema = 'public'
  and table_name = 'attendance_sessions'
  and privilege_type = 'SELECT'
order by column_name;

-- 4) Escrita direta do app em tabelas críticas (esperado: nenhuma linha)
select table_name, privilege_type
from information_schema.role_table_grants
where grantee = 'authenticated'
  and table_schema = 'public'
  and table_name in ('schedules', 'schedule_weekdays', 'schedule_exceptions',
                     'class_sessions', 'attendance_sessions', 'attendance_records',
                     'attendance_attempts', 'correction_requests', 'audit_logs')
  and privilege_type in ('INSERT', 'UPDATE', 'DELETE', 'TRUNCATE');

-- 5) Lista de tabelas criadas (esperado: 18)
select count(*) as total_de_tabelas
from pg_tables
where schemaname = 'public';
