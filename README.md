# Editor de Facas

Aplicativo Android independente extraído do módulo de facas do sistema Vendas e Produção.

## Conteúdo

- cadastro e lista de facas;
- criação manual de formas retangulares;
- captura pela câmera com marcador ArUco;
- correção de perspectiva e extração do contorno com OpenCV;
- editor completo de contorno;
- medição de referência;
- encaixe e cálculo de consumo;
- armazenamento local com Room;
- importação e exportação dos projetos.

O aplicativo usa o identificador `com.musgo.editordefacas`, portanto pode ser instalado junto
com o aplicativo original.

## Integração com o aplicativo principal

1. Cadastre e edite a faca neste aplicativo.
2. Conclua o encaixe e o cálculo de consumo.
3. Na lista, use o ícone de exportação para salvar o arquivo `.bloco`.
4. No aplicativo principal, abra **Nova Faca** e escolha **Importar do Editor de Facas**.

O pacote transfere cadastro, contorno, área, encaixe, consumo e foto. O aplicativo principal
apenas recebe e consulta esses dados; a edição geométrica e os novos cálculos permanecem neste
Editor de Facas independente.

## Compilar

No Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

APK gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Observação sobre IA

A extração por OpenCV funciona de forma independente. O recurso opcional de IA continua no
código, mas precisa receber uma autenticação própria antes de ser usado fora do aplicativo
original.
