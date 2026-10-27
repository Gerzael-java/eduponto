# EduPonto — backend (Supabase)

Esta pasta contém o banco de dados do EduPonto e suas regras de segurança.
A decisão de usar o Supabase está em `docs/DECISOES-TECNICAS.md`.

## Estrutura

```
backend/
├── README.md
└── supabase/
    ├── migrations/                        migrações versionadas (rodar em ordem)
    │   ├── 20261026000100_core_schema.sql        tabelas, restrições e índices
    │   ├── 20261026000200_security_and_audit.sql RLS, privilégios e auditoria
    │   └── 20261026000300_auth_helpers.sql       apoio ao login (troca de senha)
    ├── bootstrap/
    │   └── primeiro_administrador.sql.exemplo    modelo para criar o 1º administrador
    └── verificacao/
        └── 01_verificar_seguranca.sql            confere a segurança depois de aplicar
```

## Como aplicar (primeira vez)

1. Crie uma conta em supabase.com e um projeto novo (plano **Free**).
   Escolha uma região próxima do Brasil e guarde a senha do banco em um gerenciador
   de senhas. Ela **nunca** vai para o GitHub.
2. No painel, em **Authentication**, desative o cadastro público de novos usuários
   (o nome da opção pode variar, algo como "Allow new users to sign up"). Os usuários
   são criados pelo administrador.
3. Abra **SQL Editor**, cole e execute, **nesta ordem**:
   1. `20261026000100_core_schema.sql`
   2. `20261026000200_security_and_audit.sql`
   3. `20261026000300_auth_helpers.sql`
4. Em **Authentication > Users > Add user**, crie o usuário do administrador
   (e-mail e senha, com "Auto Confirm User").
5. Copie `bootstrap/primeiro_administrador.sql.exemplo`, troque os valores entre `<...>`
   e execute no SQL Editor (não salve a versão preenchida no repositório).
6. Execute `verificacao/01_verificar_seguranca.sql` e confira os resultados esperados,
   que estão comentados no próprio arquivo.

> Alternativa para quem usa computador: a CLI do Supabase aplica as migrações com
> `supabase db push`. Para o começo, o SQL Editor é suficiente.

## Segredos: o que pode e o que não pode ir para o app

| Item | Pode ir no app? |
|---|---|
| URL do projeto | Sim (fica em `local.properties`, fora do GitHub) |
| Chave pública (`anon` / `publishable`) | Sim, é feita para isso (também fora do GitHub) |
| Chave `service_role` / secreta | **Nunca.** Só em funções de servidor |
| Senha do banco | **Nunca** |

## Observações importantes

- O app **não lê `*` de `attendance_sessions`**: os hashes do token são protegidos e a
  consulta precisa listar as colunas permitidas (ver `docs/DECISOES-TECNICAS.md`).
- No plano gratuito o projeto **pausa após 1 semana sem uso**. Serve para a fase de
  testes, mas não para uso real com alunos.
- Estas migrações **não foram executadas** por quem as escreveu (sem acesso a um
  Postgres). A primeira execução é o teste: se algum comando falhar, envie a mensagem.
