// ========================= src/view/ImageUtil.java (VERSÃO FINAL REFINADA) =========================
package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Utilitário para carregar e redimensionar imagens/ícones do projeto.
 * Procura na ordem:
 * 1) classpath: /resources/<filename>
 * 2) classpath: /<filename>
 * 3) disco:     resources/<filename>
 *
 * Mantém um cache LRU por (filename|size) de ImageIcon escalado com alta qualidade.
 */
public final class ImageUtil {

    private static final String CLASSPATH_PREFIX = "/resources/";
    private static final String FILE_PREFIX = "resources" + File.separator;
    private static final int MAX_CACHE = 256;

    // Cache LRU simples e sincronizado
    private static final Map<String, ImageIcon> ICON_CACHE =
            new LinkedHashMap<>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, ImageIcon> eldest) {
                    return size() > MAX_CACHE;
                }
            };

    private ImageUtil() { /* classe utilitária */ }

    /**
     * Retorna um ImageIcon da peça (K,Q,R,B,N,P) para a cor indicada.
     * Se não encontrar a imagem, gera um placeholder legível.
     */
    public static ImageIcon getPieceIcon(boolean isWhite, char pieceChar, int size) {
        char p = Character.toUpperCase(pieceChar);
        if ("KQRBNP".indexOf(p) < 0) {
            return placeholderIcon('?', isWhite, sanitizeSize(size));
        }
        String prefix = isWhite ? "w" : "b";
        String filename = prefix + p + ".png";
        ImageIcon icon = getIcon(filename, size);
        if (icon == null) {
            return placeholderIcon(p, isWhite, sanitizeSize(size));
        }
        return icon;
    }

    /**
     * Carrega um ImageIcon do resources, redimensionando para size x size com alta qualidade.
     * Usa cache LRU para evitar reprocessamento.
     */
    public static ImageIcon getIcon(String filename, int size) {
        size = sanitizeSize(size);
        String cacheKey = filename + "|" + size;
        synchronized (ImageUtil.class) {
            ImageIcon cached = ICON_CACHE.get(cacheKey);
            if (cached != null) return cached;
        }

        BufferedImage img = loadBuffered(filename);
        if (img == null) return null;

        BufferedImage scaled = scaleImageHQ(img, size, size);
        ImageIcon icon = new ImageIcon(scaled);

        synchronized (ImageUtil.class) {
            ICON_CACHE.put(cacheKey, icon);
        }
        return icon;
    }

    /**
     * Tenta carregar a imagem como BufferedImage de vários locais.
     */
    public static BufferedImage loadBuffered(String filename) {
        if (filename == null || filename.isEmpty()) return null;

        // 1) Classpath com prefixo /resources/
        try {
            URL url = ImageUtil.class.getResource(CLASSPATH_PREFIX + filename);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (Exception e) {
            // <<<<<<< SUGESTÃO: Imprimir erro para facilitar a depuração >>>>>>>>>
            System.err.println("Falha ao carregar imagem (recurso): " + CLASSPATH_PREFIX + filename);
            e.printStackTrace();
        }

        // 2) Classpath raiz
        try {
            URL url = ImageUtil.class.getResource("/" + filename);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (Exception e) {
            System.err.println("Falha ao carregar imagem (raiz): /" + filename);
            e.printStackTrace();
        }

        // 3) Arquivo local
        try {
            File f = new File(FILE_PREFIX + filename);
            if (f.exists()) {
                return ImageIO.read(f);
            }
        } catch (Exception e) {
            System.err.println("Falha ao carregar imagem (arquivo): " + FILE_PREFIX + filename);
            e.printStackTrace();
        }

        return null; // não encontrado
    }

    /**
     * Gera um ícone placeholder com fundo e letra.
     */
    public static ImageIcon placeholderIcon(char pieceChar, boolean isWhite, int size) {
        size = sanitizeSize(size);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color bg = isWhite ? new Color(245, 245, 245) : new Color(60, 60, 60);
            Color fg = isWhite ? new Color(25, 25, 25) : new Color(230, 230, 230);

            g.setColor(bg);
            g.fillRoundRect(0, 0, size, size, size / 6, size / 6);

            g.setColor(isWhite ? new Color(200, 200, 200) : new Color(40, 40, 40));
            g.drawRoundRect(0, 0, size - 1, size - 1, size / 6, size / 6);

            g.setColor(fg);
            int fontSize = Math.max(12, (int) (size * 0.55));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
            FontMetrics fm = g.getFontMetrics();
            String s = String.valueOf(Character.toUpperCase(pieceChar));
            int textW = fm.stringWidth(s);
            int textH = fm.getAscent();

            int x = (size - textW) / 2;
            int y = (size + textH) / 2 - Math.max(2, size / 30);
            g.drawString(s, x, y);
        } finally {
            g.dispose();
        }
        return new ImageIcon(img);
    }

    private static int sanitizeSize(int size) {
        if (size <= 0) return 1;
        return Math.min(size, 1024);
    }

    private static BufferedImage scaleImageHQ(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        return dst;
    }
}