# EduPonto — decisões técnicas

Registro das decisões de arquitetura. Cada item diz **o que** foi decidido e **por quê**.

## D1. Backend: Supabase (Postgres + Auth + regras por linha)
O modelo de dados é relacional (chaves estrangeiras, unicidade, relatórios de frequência)
e a autorização precisa ficar em uma camada confiável. O Supabase oferece as duas coisas
no próprio banco. Alternativas avaliadas: Firebase (banco de documentos; funções de servidor
exigem plano pago) e API própria (exige hospedar e manter servidor).
Riscos: o plano gratuito pausa o projeto após 1 semana sem uso, e a biblioteca Kotlin
(supabase-kt) é mantida pela comunidade.

## D2. Uma única instituição
Sem `organization_id` nas tabelas. As configurações ficam em `institution_settings`,
que só aceita uma linha. Se um dia houver várias instituições, será uma migração própria.

## D3. Contas criadas pelo administrador
Não há cadastro público. O administrador cria usuários (e-mail e senha provisória) por uma
função de servidor que usa a chave secreta, que nunca vai para o app. O usuário troca a senha
no primeiro acesso (`users.must_change_password`). O primeiro administrador é criado
manualmente (`bootstrap/`).

## D4. Identificadores e tipos
Todos os registros usam `uuid`. Instantes são `timestamptz` (UTC); horários de aula são
`time` interpretados no fuso da instituição (`institution_settings.timezone`). Valores de
domínio são `text` com `CHECK` (mais fácil de evoluir que `enum`).

## D5. Nada é apagado por acidente
Todas as chaves estrangeiras são `ON DELETE RESTRICT`. Usuários, turmas e matrículas são
desativados ou encerrados, nunca removidos, e o histórico de presenças permanece.

## D6. Autorização no banco (RLS), nunca só na interface
- O papel `anon` não tem acesso a nada.
- Tabelas críticas (horários, ocorrências, chamadas, presenças, correções, auditoria) são
  **somente leitura** para o app; toda alteração passa por funções de servidor (RPC) que
  validam permissão, regras e conflitos na mesma transação.
- Estrutura administrativa (turmas, matrículas, papéis...) é escrita direto, mas limitada
  por políticas que exigem o papel de administrador.

## D7. Chamadas seguras
O token do QR e o código alternativo só existem em claro para o professor, uma vez, quando
são gerados; no banco fica apenas o hash. Os hashes não são legíveis pelo app (privilégio por
coluna). O registro de presença é idempotente (`client_request_id` único) e protegido por
índice único (um registro ativo por aluno e chamada), o que vale mesmo com requisições
simultâneas. Tentativas são gravadas em `attendance_attempts` para limitar tentativas
automatizadas. A hora oficial do registro é a do servidor.

## D8. Auditoria imutável
Triggers registram inserções, alterações e exclusões das tabelas relevantes em `audit_logs`,
com estado anterior e novo, e responsável. Dados pessoais (e-mail, matrícula) e hashes são
removidos do registro. A tabela não aceita UPDATE, DELETE nem TRUNCATE.

## D9. Proteção do último administrador
Triggers impedem remover o papel de administrador, ou desativar o usuário, se ele for o
último administrador ativo.

## D10. Registrar presença exige internet
O token precisa ser validado pelo servidor, e o modo offline não pode contornar validade,
autenticação nem duplicidade. Sem conexão, o app apenas consulta dados já baixados (cache
local em Room). Presença offline só entraria depois, com tokens assinados e revalidação.

## D11. Dados locais e migrações
O Room passa a ser apenas cache do servidor. A partir da integração com o Supabase, o banco
local usa **migrações reais** (sem `fallbackToDestructiveMigration`). Os dados de teste atuais
são descartados uma única vez, porque a identificação passa de nomes para identificadores
únicos.

## D12. Consultas do app em `attendance_sessions`
Como os hashes são protegidos por privilégio de coluna, o app deve sempre listar as colunas
(`id, class_session_id, teacher_id, status, opened_at, closed_at, code_validity_seconds,
token_version, token_expires_at, created_at`); `select *` falha de propósito.

## D13. Segredos
Chave secreta e senha do banco nunca entram no código nem no repositório. A URL e a chave
pública do projeto ficam em `local.properties` (ignorado pelo Git).

## D14. Login real e estado de autenticação
O login de demonstração foi removido. O app só mostra telas autenticadas depois que o servidor
valida e-mail e senha. O estado (carregando, sem sessão, trocar senha, conta bloqueada, logado)
vem do servidor e é tratado em um único ponto (`AuthGate`). Perfil e papéis são **lidos do
banco**, nunca enviados pelo app. Quem tem mais de um papel abre no de maior responsabilidade
(administrador, depois professor, depois aluno); um seletor de papel entra depois.

## D15. Senha provisória e política de senha
Contas novas nascem com `must_change_password = true`: o app obriga a trocar a senha no primeiro
acesso. A função `mark_password_changed()` é uma trava de uso, não uma barreira de segurança. O app
exige 8 caracteres, com letras e números; as regras do servidor valem também e devem ser
configuradas no painel de autenticação.

## D16. Sessão no aparelho
A biblioteca guarda a sessão no armazenamento padrão do app, **sem criptografia adicional**.
`allowBackup` está desligado, o que impede cópia de segurança dos dados. Uma versão futura deve
guardar a sessão em armazenamento criptografado (Keystore).

## D17. Configuração e segredos no app
URL e chave pública do Supabase vêm de `local.properties` (fora do Git) ou de variáveis de ambiente
no CI. O app recusa iniciar com uma chave que pareça secreta (`sb_secret_...` ou JWT com papel
`service_role`).

## D18. Textos e tema
Textos novos ficam em `strings.xml`; mensagens de erro do domínio são códigos (`AuthError`)
traduzidos na interface. O tema escuro é o padrão, com cores, formas e espaçamentos centralizados
em `ui/theme`. As telas antigas (horários, chamadas, histórico) serão migradas aos poucos.

## D19. Funcionalidades locais em transição
Horários, chamadas e presenças ainda usam o banco local (Room) e a identificação por nome, vinda
do login real. São provisórios e serão substituídos por versões ligadas ao servidor nas próximas
etapas.

## Pendências conhecidas
- Funções de servidor (RPC) para horários, chamadas, presenças e correções.
- Função de servidor (Edge Function) para o administrador criar usuários.
- Seletor de papel para quem tem mais de um.
- Sessão em armazenamento criptografado.
- Verificar o que o plano pago oferece de backup antes do uso real.
- Avaliação de LGPD (alunos menores de idade) e do uso para controle de jornada.
