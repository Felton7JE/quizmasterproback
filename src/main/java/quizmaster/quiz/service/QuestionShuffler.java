package quizmaster.quiz.service;

import quizmaster.quiz.models.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Baralha as opções de uma pergunta de forma determinística (a partir de uma semente),
 * para que a posição da resposta correta varie entre perguntas/jogos mas seja a mesma
 * para todos os jogadores do mesmo grupo (mesmo jogo + mesma pergunta).
 *
 * A permutação devolvida segue a convenção: perm[novaPosicao] = posicaoOriginal.
 * O servidor recalcula a permutação sempre que precisa (servir a pergunta, validar
 * a resposta, rever respostas), por isso não é necessário guardar nada na BD.
 */
public final class QuestionShuffler {

    private QuestionShuffler() {
    }

    /** Semente estável para (jogo, pergunta): igual para todos os jogadores do mesmo jogo. */
    public static long seedFor(Long gameId, Long questionId) {
        long g = gameId != null ? gameId : 0L;
        long q = questionId != null ? questionId : 0L;
        return g * 1_000_003L + q * 7_919L + 17L;
    }

    /** Semente aleatória (modos sem sincronização entre jogadores, ex.: solo/livre). */
    public static long randomSeed() {
        return new Random().nextLong();
    }

    public static int[] permutation(int size, long seed) {
        int[] perm = new int[Math.max(size, 0)];
        for (int i = 0; i < perm.length; i++) {
            perm[i] = i;
        }
        Random rnd = new Random(seed);
        for (int i = perm.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;
        }
        return perm;
    }

    public static int[] permutation(Question question, long seed) {
        int size = question.getOptions() != null ? question.getOptions().size() : 0;
        return permutation(size, seed);
    }

    /** Opções na nova ordem. */
    public static List<String> shuffledOptions(Question question, int[] perm) {
        List<String> original = question.getOptions();
        if (original == null) {
            return null;
        }
        List<String> result = new ArrayList<>(original.size());
        for (int newPos = 0; newPos < perm.length && newPos < original.size(); newPos++) {
            result.add(original.get(perm[newPos]));
        }
        return result;
    }

    /** Índice da resposta correta na nova ordem (ou o original se não for possível calcular). */
    public static Integer shuffledCorrectIndex(Question question, int[] perm) {
        Integer correct = question.getCorrectAnswer();
        if (correct == null) {
            return null;
        }
        for (int newPos = 0; newPos < perm.length; newPos++) {
            if (perm[newPos] == correct) {
                return newPos;
            }
        }
        return correct;
    }

    /** Converte o índice escolhido pelo jogador (ordem baralhada) para o índice original. */
    public static Integer toOriginalIndex(int[] perm, Integer shuffledIndex) {
        if (shuffledIndex == null || shuffledIndex < 0 || shuffledIndex >= perm.length) {
            return null;
        }
        return perm[shuffledIndex];
    }
}
