# RoyalChess

## Descrição Geral

RoyalChess é um jogo de xadrez completo desenvolvido em Java, com interface gráfica (Swing), IA de múltiplos níveis, placar de peças capturadas, histórico de jogadas e suporte a todas as regras oficiais do xadrez. O projeto é modularizado em pacotes para facilitar manutenção e expansão.

---

## Funcionalidades Principais

- **Interface Gráfica Moderna:**
	- Tabuleiro 8x8 com botões interativos.
	- Peças exibidas por imagens PNG (em `/resources`) ou Unicode.
	- Destaque de seleção, movimentos legais e último lance.
	- Placar de peças capturadas para ambos os lados.
	- Histórico de jogadas com rolagem.
	- Barra de status com indicação de vez, xeque e fim de jogo.

- **Menu e Controles:**
	- Novo jogo.
	- Sair do jogo.
	- Alternar se o PC joga com as pretas.
	- Seleção de nível de dificuldade da IA (Fácil, Médio, Difícil).

- **Regras Oficiais do Xadrez:**
	- Movimentação completa de todas as peças.
	- Promoção de peão.
	- Roque, en passant, xeque, xeque-mate e empate.

- **Placar de Capturas:**
	- Mostra as peças capturadas de cada lado, atualizando em tempo real.

- **Histórico de Jogadas:**
	- Exibe todos os lances realizados, numerados e formatados.

---

## Inteligência Artificial (IA)

- **Fácil:**
	- Joga movimentos aleatórios válidos.
- **Médio:**
	- Prioriza capturas e casas centrais.
- **Difícil:**
	- Utiliza algoritmo Minimax com Alpha-Beta pruning e busca de quiescência (implementado em `controller/ChessAI.java`).
	- Ordenação de movimentos por MVV-LVA (capturas mais valiosas primeiro).
	- Avaliação baseada em material e posição.

A IA pode ser ativada para jogar com as pretas via menu ou checkbox lateral.

---

## Estrutura do Projeto

```
resources/         # Imagens PNG das peças (bK.png, wQ.png, etc)
src/
	ai/              # Algoritmos de IA (níveis, utilitários)
	controller/      # Lógica do jogo, ChessAI, controle de partidas
	model/
		board/         # Representação do tabuleiro, movimentos, posições
		pieces/        # Classes das peças (Rei, Rainha, etc)
	view/            # Interface gráfica (ChessGUI, utilitários de imagem)
```

---

## Como Executar

1. Compile todo o projeto Java (recomendado: use uma IDE como IntelliJ ou VS Code com suporte a Java).
2. Execute a classe principal:
	 - `view/ChessGUI.java`
3. O jogo abrirá uma janela gráfica pronta para jogar.

---

## Observações Técnicas

- O código é orientado a objetos e modular.
- O tabuleiro e as peças são atualizados em tempo real.
- O histórico e o placar de capturas são persistentes durante a partida.
- O projeto pode ser expandido para incluir mais níveis de IA, análise de partidas, exportação PGN, etc.

---

## Créditos

Desenvolvido por João Felipe e colaboradores.

Imagens das peças: domínio público.

---

## Licença

Este projeto é livre para uso acadêmico e pessoal.
