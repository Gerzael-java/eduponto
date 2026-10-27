# EduPonto

Aplicativo Android (Kotlin + Jetpack Compose + Material 3) para controle de horários e presenças
de professores e alunos. **Versão de testes**: tudo fica salvo apenas no aparelho, sem servidor.

## Funções

- Login de demonstração (sem senha) com perfil de professor ou aluno
- Professor: cadastra horários, inicia e encerra chamadas, exibe o QR Code da chamada,
  lê o comprovante do aluno e consulta o histórico
- Aluno: lê o QR Code da chamada, recebe um QR de comprovante e consulta o histórico
- Banco de dados local (Room), leitura de QR com CameraX + ML Kit, geração de QR com ZXing

## Fluxo de presença (duas vias, sem servidor)

1. O professor inicia a chamada e mostra o QR Code.
2. O aluno lê o QR e a presença é registrada no celular dele.
3. O aluno mostra o QR de comprovante ao professor.
4. O professor lê o comprovante e a presença é confirmada no celular dele.

## Limitações (importante)

- Não há servidor: cada celular guarda apenas os próprios dados, e nada é sincronizado.
- O login não é seguro: qualquer pessoa pode entrar com qualquer nome e perfil.
- Os QR Codes não têm assinatura: um comprovante falso pode ser criado por quem entender o formato.
- O QR da chamada pode ser fotografado e repassado a quem não está presente.
- A validade das chamadas (20 minutos) depende do relógio do aparelho.
- Os dados não são criptografados e são apagados ao desinstalar o app.
- Nesta versão de testes, uma mudança no esquema do banco apaga os dados antigos.
- O APK é de depuração, assinado com a chave de debug, apenas para testes.

## Versões usadas

Supabase (supabase-kt 3.6.0) · JDK 17 · Gradle 9.4.1 · Android Gradle Plugin 9.2.1 · Kotlin 2.3.21 · KSP 2.3.7 ·
Compose BOM 2026.08.00 · Room 2.8.4 · CameraX 1.6.1 · ML Kit barcode-scanning 17.3.0 ·
compileSdk 37 · targetSdk 36 · minSdk 26. Todas as versões ficam em `gradle/libs.versions.toml`.

## Configurar o servidor (Supabase)

O app precisa da URL e da chave **pública** do projeto Supabase. Guarde-as em `local.properties`
(arquivo que não vai para o GitHub):

```bash
bash scripts/configurar-supabase.sh
```

Nunca use a chave secreta (`service_role` / `sb_secret_...`) no app. O banco está em `backend/`
(ver `backend/README.md`) e as decisões de arquitetura em `docs/DECISOES-TECNICAS.md`.

## Testes automatizados

```bash
./gradlew testDebugUnitTest
```

## Gerar o APK

**No GitHub Codespaces** (ambiente preparado por `.devcontainer/setup.sh`):

```bash
bash scripts/build-apk.sh
```

O arquivo fica em `app/build/outputs/apk/debug/app-debug.apk`.

**Pelo GitHub Actions** (sem abrir o Codespace): aba **Actions** > **Gerar APK de depuração** >
**Run workflow**. Ao terminar, baixe o arquivo `EduPonto-debug-apk` na própria execução.

**Publicar como Release:**

```bash
bash scripts/release.sh v0.5.0 "Descrição do release"
```

## Instalar no celular

Baixe o `app-debug.apk`, abra-o pelo app de arquivos, permita a instalação de apps desconhecidos
quando o Android pedir e, se o Play Protect avisar, escolha "Instalar mesmo assim".

## Estrutura

`app/src/main/java/br/com/eduponto/`: `domain` (modelos, repositórios, casos de uso), `data` (Room),
`ui` (tema e componentes), `navigation`, `professores`, `alunos`, `horarios`, `chamadas`,
`qrcode` e `presencas`.
