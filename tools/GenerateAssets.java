import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Génère les images, les sons et les icônes du jeu (œuvres originales, produites par code : aucun droit d'auteur
 * tiers).
 *
 * Utilisation, depuis la racine du projet :
 *     java -Djava.awt.headless=true tools/GenerateAssets.java
 */
public class GenerateAssets {

    static final Path RESOURCES = Path.of("src/main/resources");
    static final Path PACKAGING = Path.of("packaging");
    static final float SAMPLE_RATE = 22050f;

    public static void main(String[] args) throws IOException {
        Files.createDirectories(RESOURCES);
        Files.createDirectories(PACKAGING);

        write("ghost.png", ghost(160));
        write("icon.png", ghost(64));
        write("melee.png", warrior(160));
        write("stealth.png", ninja(160));
        write("tank.png", knight(160));
        write("coin.png", coin(128));
        write("background.png", background(600, 400));

        sound("jump.wav", 0.12, t -> Math.sin(2 * Math.PI * (320 * t + 1583 * t * t)) * decay(t, 0.12));
        sound("shoot.wav", 0.12, t -> (0.5 * noise() + 0.5 * square(800 * t - 3000 * t * t)) * decay(t, 0.12));
        sound("coin.wav", 0.24, t -> t < 0.07
                ? Math.sin(2 * Math.PI * 988 * t) * Math.exp(-t * 14)
                : Math.sin(2 * Math.PI * 1319 * t) * Math.exp(-(t - 0.07) * 16));
        sound("eliminated.wav", 0.2, t -> triangle(700 * t - 1300 * t * t) * decay(t, 0.2));
        sound("damage.wav", 0.25, t -> (0.6 * sawtooth(120 * t) + 0.4 * noise()) * decay(t, 0.25));
        sound("gameover.wav", 0.8, t -> {
            double[] notes = {392, 330, 262, 196};
            int i = Math.min(3, (int) (t / 0.2));
            double vibrato = 1 + 0.01 * Math.sin(2 * Math.PI * 7 * t);
            return square(notes[i] * vibrato * t) * 0.6 * Math.exp(-(t - i * 0.2) * 5) * decay(t, 0.8);
        });

        writeAppIcons();

        System.out.println("Assets generated in " + RESOURCES.toAbsolutePath() + " and " + PACKAGING.toAbsolutePath());
    }

    // ------------------------------------------------------------------ images

    static BufferedImage image(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    static Graphics2D graphics(BufferedImage img, double scale) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.scale(scale, scale);
        return g;
    }

    static void write(String name, BufferedImage img) throws IOException {
        ImageIO.write(img, "png", RESOURCES.resolve(name).toFile());
    }

    /** Le fantôme jouable (repère de dessin : 160 x 160). */
    static BufferedImage ghost(int size) {
        BufferedImage img = image(size, size);
        Graphics2D g = graphics(img, size / 160.0);
        drawGhost(g);
        g.dispose();
        return img;
    }

    static void drawGhost(Graphics2D g) {
        // halo
        g.setPaint(new RadialGradientPaint(new Point2D.Float(80, 76), 74, new float[]{0f, 1f},
                new Color[]{new Color(170, 200, 255, 110), new Color(170, 200, 255, 0)}));
        g.fill(new Ellipse2D.Double(2, 2, 156, 156));

        // corps : dôme + bas festonné
        Path2D body = new Path2D.Double();
        body.moveTo(30, 132);
        body.lineTo(30, 72);
        body.append(new Arc2D.Double(30, 22, 100, 100, 180, -180, Arc2D.OPEN), true);
        body.lineTo(130, 132);
        double[] xs = {130, 96.67, 63.33, 30};
        for (int i = 0; i < 3; i++) {
            body.quadTo((xs[i] + xs[i + 1]) / 2, 156, xs[i + 1], 132);
        }
        body.closePath();
        g.setPaint(new GradientPaint(0, 30, new Color(255, 255, 255), 0, 145, new Color(196, 212, 244)));
        g.fill(body);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(140, 160, 205));
        g.draw(body);

        // joues
        g.setColor(new Color(255, 150, 170, 120));
        g.fill(new Ellipse2D.Double(38, 88, 18, 11));
        g.fill(new Ellipse2D.Double(104, 88, 18, 11));

        // yeux
        g.setColor(new Color(25, 25, 45));
        g.fill(new Ellipse2D.Double(56, 60, 17, 27));
        g.fill(new Ellipse2D.Double(87, 60, 17, 27));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(60, 65, 6, 8));
        g.fill(new Ellipse2D.Double(91, 65, 6, 8));

        // sourcils froncés et sourire en coin
        g.setColor(new Color(25, 25, 45));
        g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(50, 50, 74, 58));
        g.draw(new Line2D.Double(110, 50, 86, 58));
        g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(64, 84, 32, 20, 200, 140, Arc2D.OPEN));
    }

    /** Héros « corps-à-corps » : guerrier au casque et à l'épée. */
    static BufferedImage warrior(int size) {
        BufferedImage img = image(size, size);
        Graphics2D g = graphics(img, size / 160.0);

        // pieds
        g.setColor(new Color(80, 50, 35));
        g.fill(new Ellipse2D.Double(44, 134, 32, 16));
        g.fill(new Ellipse2D.Double(84, 134, 32, 16));

        // tunique
        g.setPaint(new GradientPaint(0, 104, new Color(230, 70, 60), 0, 144, new Color(160, 35, 35)));
        g.fill(new RoundRectangle2D.Double(46, 104, 68, 42, 30, 30));
        g.setColor(new Color(240, 200, 70));
        g.fill(new RoundRectangle2D.Double(46, 120, 68, 7, 4, 4));

        // épée (derrière la main droite)
        AffineTransform before = g.getTransform();
        g.rotate(Math.toRadians(22), 128, 112);
        g.setPaint(new GradientPaint(120, 0, new Color(235, 240, 250), 134, 0, new Color(150, 160, 180)));
        g.fill(new RoundRectangle2D.Double(121, 26, 14, 78, 5, 5));
        Path2D tip = new Path2D.Double();
        tip.moveTo(121, 28);
        tip.lineTo(128, 10);
        tip.lineTo(135, 28);
        tip.closePath();
        g.fill(tip);
        g.setColor(new Color(235, 190, 60));
        g.fill(new RoundRectangle2D.Double(112, 102, 32, 8, 4, 4));
        g.setColor(new Color(110, 70, 40));
        g.fill(new RoundRectangle2D.Double(124, 108, 8, 22, 3, 3));
        g.setTransform(before);

        // tête
        g.setPaint(new GradientPaint(0, 28, new Color(255, 224, 190), 0, 112, new Color(238, 186, 150)));
        g.fill(new Ellipse2D.Double(38, 28, 84, 84));

        // casque
        Area helmet = new Area(new Arc2D.Double(36, 26, 88, 88, 0, 180, Arc2D.CHORD));
        g.setPaint(new GradientPaint(0, 26, new Color(215, 222, 235), 0, 70, new Color(120, 132, 155)));
        g.fill(helmet);
        g.setColor(new Color(90, 100, 125));
        g.fill(new RoundRectangle2D.Double(36, 62, 88, 9, 6, 6));
        // plumet
        g.setColor(new Color(215, 45, 55));
        g.fill(new Ellipse2D.Double(70, 12, 20, 30));

        // visage
        g.setColor(new Color(40, 30, 30));
        g.fill(new Ellipse2D.Double(58, 80, 9, 12));
        g.fill(new Ellipse2D.Double(93, 80, 9, 12));
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(54, 74, 68, 79));
        g.draw(new Line2D.Double(106, 74, 92, 79));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(69, 96, 22, 10, 200, 140, Arc2D.OPEN));

        g.dispose();
        return img;
    }

    /** Héros « furtif » : ninja encapuchonné avec un shuriken. */
    static BufferedImage ninja(int size) {
        BufferedImage img = image(size, size);
        Graphics2D g = graphics(img, size / 160.0);

        g.setColor(new Color(20, 18, 35));
        g.fill(new Ellipse2D.Double(46, 136, 28, 14));
        g.fill(new Ellipse2D.Double(86, 136, 28, 14));

        // corps
        g.setPaint(new GradientPaint(0, 100, new Color(70, 55, 120), 0, 146, new Color(35, 25, 70)));
        g.fill(new RoundRectangle2D.Double(44, 100, 72, 46, 34, 34));
        // écharpe
        g.setColor(new Color(215, 50, 70));
        g.fill(new RoundRectangle2D.Double(46, 100, 68, 10, 6, 6));
        Path2D tail = new Path2D.Double();
        tail.moveTo(100, 104);
        tail.lineTo(142, 92);
        tail.lineTo(136, 108);
        tail.lineTo(108, 112);
        tail.closePath();
        g.fill(tail);

        // capuche
        g.setPaint(new GradientPaint(0, 22, new Color(95, 75, 160), 0, 110, new Color(42, 32, 88)));
        g.fill(new Ellipse2D.Double(34, 22, 92, 92));

        // ouverture du visage
        g.setColor(new Color(14, 12, 24));
        g.fill(new RoundRectangle2D.Double(46, 56, 68, 34, 30, 30));
        // yeux lumineux
        g.setColor(new Color(255, 240, 140));
        Path2D leftEye = new Path2D.Double();
        leftEye.moveTo(54, 66);
        leftEye.lineTo(76, 72);
        leftEye.lineTo(72, 80);
        leftEye.lineTo(56, 76);
        leftEye.closePath();
        g.fill(leftEye);
        Path2D rightEye = new Path2D.Double();
        rightEye.moveTo(106, 66);
        rightEye.lineTo(84, 72);
        rightEye.lineTo(88, 80);
        rightEye.lineTo(104, 76);
        rightEye.closePath();
        g.fill(rightEye);

        // bandeau métallique
        g.setPaint(new GradientPaint(0, 44, new Color(225, 230, 240), 0, 56, new Color(130, 140, 160)));
        g.fill(new RoundRectangle2D.Double(38, 42, 84, 13, 5, 5));
        g.setColor(new Color(90, 100, 120));
        g.fill(new Ellipse2D.Double(46, 46, 5, 5));
        g.fill(new Ellipse2D.Double(109, 46, 5, 5));

        // shuriken
        Path2D shuriken = new Path2D.Double();
        double cx = 24, cy = 112, outer = 20, inner = 6.5;
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i - Math.PI / 2;
            double radius = (i % 2 == 0) ? outer : inner;
            double px = cx + radius * Math.cos(angle), py = cy + radius * Math.sin(angle);
            if (i == 0) shuriken.moveTo(px, py); else shuriken.lineTo(px, py);
        }
        shuriken.closePath();
        g.setPaint(new GradientPaint(4, 92, new Color(240, 244, 252), 44, 132, new Color(120, 130, 150)));
        g.fill(shuriken);
        g.setColor(new Color(60, 66, 84));
        g.fill(new Ellipse2D.Double(cx - 4, cy - 4, 8, 8));

        g.dispose();
        return img;
    }

    /** Héros « tank » : chevalier lourd avec un bouclier. */
    static BufferedImage knight(int size) {
        BufferedImage img = image(size, size);
        Graphics2D g = graphics(img, size / 160.0);

        g.setColor(new Color(40, 45, 60));
        g.fill(new Ellipse2D.Double(46, 138, 30, 14));
        g.fill(new Ellipse2D.Double(90, 138, 30, 14));

        // corps
        g.setPaint(new GradientPaint(0, 84, new Color(170, 182, 205), 0, 146, new Color(84, 96, 125)));
        g.fill(new RoundRectangle2D.Double(38, 84, 88, 62, 26, 26));
        g.setColor(new Color(235, 190, 60));
        g.fill(new RoundRectangle2D.Double(38, 112, 88, 8, 4, 4));

        // casque
        g.setPaint(new GradientPaint(0, 28, new Color(200, 210, 228), 0, 96, new Color(100, 112, 140)));
        g.fill(new Ellipse2D.Double(42, 28, 80, 76));
        // cornes
        g.setColor(new Color(245, 235, 210));
        Path2D leftHorn = new Path2D.Double();
        leftHorn.moveTo(46, 52); leftHorn.lineTo(28, 30); leftHorn.lineTo(58, 42); leftHorn.closePath();
        g.fill(leftHorn);
        Path2D rightHorn = new Path2D.Double();
        rightHorn.moveTo(118, 52); rightHorn.lineTo(136, 30); rightHorn.lineTo(106, 42); rightHorn.closePath();
        g.fill(rightHorn);
        // visière
        g.setColor(new Color(18, 20, 30));
        g.fill(new RoundRectangle2D.Double(52, 58, 60, 16, 10, 10));
        g.setColor(new Color(255, 120, 50));
        g.fill(new Ellipse2D.Double(64, 62, 9, 8));
        g.fill(new Ellipse2D.Double(91, 62, 9, 8));
        // crête
        g.setColor(new Color(215, 45, 55));
        g.fill(new RoundRectangle2D.Double(76, 22, 10, 22, 5, 5));

        // bouclier
        Path2D shield = new Path2D.Double();
        shield.moveTo(8, 70);
        shield.lineTo(54, 70);
        shield.lineTo(54, 108);
        shield.quadTo(54, 142, 31, 156);
        shield.quadTo(8, 142, 8, 108);
        shield.closePath();
        g.setPaint(new GradientPaint(8, 70, new Color(80, 130, 225), 54, 150, new Color(40, 70, 160)));
        g.fill(shield);
        g.setColor(new Color(235, 190, 60));
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shield);
        g.fill(new RoundRectangle2D.Double(28.5, 78, 5, 62, 3, 3));
        g.fill(new RoundRectangle2D.Double(14, 96, 34, 5, 3, 3));

        g.dispose();
        return img;
    }

    /** La pièce d'or (repère de dessin : 128 x 128). */
    static BufferedImage coin(int size) {
        BufferedImage img = image(size, size);
        Graphics2D g = graphics(img, size / 128.0);

        g.setPaint(new RadialGradientPaint(new Point2D.Float(50, 46), 84, new float[]{0f, 1f},
                new Color[]{new Color(255, 240, 150), new Color(222, 158, 18)}));
        g.fill(new Ellipse2D.Double(8, 8, 112, 112));
        g.setStroke(new BasicStroke(6f));
        g.setColor(new Color(176, 112, 10));
        g.draw(new Ellipse2D.Double(9, 9, 110, 110));
        g.setStroke(new BasicStroke(3.5f));
        g.setColor(new Color(255, 218, 90));
        g.draw(new Ellipse2D.Double(24, 24, 80, 80));

        // étoile en relief
        Path2D star = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 5 * i - Math.PI / 2;
            double radius = (i % 2 == 0) ? 30 : 12.5;
            double px = 64 + radius * Math.cos(angle), py = 65 + radius * Math.sin(angle);
            if (i == 0) star.moveTo(px, py); else star.lineTo(px, py);
        }
        star.closePath();
        g.setColor(new Color(186, 126, 16));
        g.fill(star);
        g.setColor(new Color(255, 232, 130));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(star);

        // reflet
        g.setColor(new Color(255, 255, 255, 120));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(16, 16, 96, 96, 120, 60, Arc2D.OPEN));

        g.dispose();
        return img;
    }

    /**
     * Décor de nuit. L'image se raccorde à elle-même horizontalement (toutes les formes sont périodiques
     * sur la largeur), ce qui permet le défilement continu.
     */
    static BufferedImage background(int width, int height) {
        BufferedImage img = image(width, height);
        Graphics2D g = graphics(img, 1.0);
        Random random = new Random(7);

        // ciel
        g.setPaint(new GradientPaint(0, 0, new Color(8, 10, 40), 0, height, new Color(70, 45, 120)));
        g.fillRect(0, 0, width, height);

        // étoiles
        for (int i = 0; i < 90; i++) {
            double x = random.nextDouble() * width;
            double y = random.nextDouble() * height * 0.7;
            double r = 0.6 + random.nextDouble() * 1.4;
            int alpha = 110 + random.nextInt(145);
            g.setColor(new Color(255, 255, 235, alpha));
            g.fill(new Ellipse2D.Double(x, y, r * 2, r * 2));
        }

        // lune (croissant)
        Area moon = new Area(new Ellipse2D.Double(430, 40, 70, 70));
        moon.subtract(new Area(new Ellipse2D.Double(447, 32, 66, 66)));
        g.setPaint(new GradientPaint(430, 40, new Color(255, 250, 215), 500, 110, new Color(240, 220, 150)));
        g.fill(moon);

        // nuages sombres (dessinés aussi décalés d'une largeur pour le raccord)
        double[][] clouds = {{60, 90, 1.0}, {250, 55, 0.8}, {360, 150, 1.2}, {540, 120, 0.9}};
        for (double[] c : clouds) {
            for (int shift = -1; shift <= 1; shift++) {
                cloud(g, c[0] + shift * width, c[1], c[2]);
            }
        }

        // collines lointaines
        Path2D far = new Path2D.Double();
        far.moveTo(0, height);
        for (int x = 0; x <= width; x += 4) {
            double t = 2 * Math.PI * x / width;
            far.lineTo(x, 292 + 22 * Math.sin(2 * t + 1.0) + 12 * Math.sin(5 * t + 2.0));
        }
        far.lineTo(width, height);
        far.closePath();
        g.setPaint(new GradientPaint(0, 270, new Color(52, 38, 105), 0, height, new Color(30, 22, 68)));
        g.fill(far);

        // collines proches
        Path2D near = new Path2D.Double();
        near.moveTo(0, height);
        for (int x = 0; x <= width; x += 4) {
            double t = 2 * Math.PI * x / width;
            near.lineTo(x, 340 + 14 * Math.sin(3 * t) + 7 * Math.sin(7 * t + 1.3));
        }
        near.lineTo(width, height);
        near.closePath();
        g.setColor(new Color(20, 14, 48));
        g.fill(near);

        // sapins (silhouettes) sur les collines proches
        g.setColor(new Color(14, 10, 36));
        for (int x = 10; x < width; x += 34 + random.nextInt(14)) {
            double t = 2 * Math.PI * x / width;
            double ground = 340 + 14 * Math.sin(3 * t) + 7 * Math.sin(7 * t + 1.3);
            double h = 34 + random.nextInt(30);
            for (int shift = -1; shift <= 1; shift++) {
                pine(g, x + shift * width, ground + 4, h);
            }
        }

        g.dispose();
        return img;
    }

    static void cloud(Graphics2D g, double x, double y, double scale) {
        g.setColor(new Color(120, 100, 190, 60));
        double[][] blobs = {{0, 0, 44, 22}, {28, -12, 40, 28}, {-24, -6, 34, 22}, {52, 2, 34, 18}};
        for (double[] b : blobs) {
            g.fill(new Ellipse2D.Double(x + b[0] * scale, y + b[1] * scale, b[2] * scale, b[3] * scale));
        }
    }

    static void pine(Graphics2D g, double x, double ground, double h) {
        Path2D p = new Path2D.Double();
        double w = h * 0.42;
        p.moveTo(x, ground - h);
        p.lineTo(x + w * 0.55, ground - h * 0.55);
        p.lineTo(x + w * 0.3, ground - h * 0.55);
        p.lineTo(x + w * 0.8, ground - h * 0.2);
        p.lineTo(x + w * 0.45, ground - h * 0.2);
        p.lineTo(x + w, ground);
        p.lineTo(x - w, ground);
        p.lineTo(x - w * 0.45, ground - h * 0.2);
        p.lineTo(x - w * 0.8, ground - h * 0.2);
        p.lineTo(x - w * 0.3, ground - h * 0.55);
        p.lineTo(x - w * 0.55, ground - h * 0.55);
        p.closePath();
        g.fill(p);
    }

    // ------------------------------------------------------------------ icône de l'application

    /** L'icône : un carré aux coins arrondis, ciel de nuit, avec le fantôme (repère de dessin : 160 x 160). */
    static BufferedImage appIcon(int size) {
        BufferedImage img = image(size, size);
        Graphics2D g = graphics(img, size / 160.0);

        RoundRectangle2D square = new RoundRectangle2D.Double(8, 8, 144, 144, 36, 36);
        g.setPaint(new GradientPaint(0, 8, new Color(12, 14, 56), 0, 152, new Color(84, 52, 140)));
        g.fill(square);

        // étoiles, limitées au carré arrondi
        g.setClip(square);
        Random random = new Random(21);
        for (int i = 0; i < 26; i++) {
            double x = 8 + random.nextDouble() * 144;
            double y = 8 + random.nextDouble() * 100;
            double r = 0.7 + random.nextDouble() * 1.3;
            g.setColor(new Color(255, 255, 235, 120 + random.nextInt(135)));
            g.fill(new Ellipse2D.Double(x, y, r * 2, r * 2));
        }
        g.setClip(null);

        // fantôme, légèrement réduit et centré
        g.translate(80, 84);
        g.scale(0.8, 0.8);
        g.translate(-80, -80);
        drawGhost(g);

        g.dispose();
        return img;
    }

    static byte[] png(BufferedImage img) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    static void writeAppIcons() throws IOException {
        Map<Integer, byte[]> pngs = new HashMap<>();
        for (int size : new int[]{16, 32, 48, 64, 128, 256, 512, 1024}) {
            pngs.put(size, png(appIcon(size)));
        }
        Files.write(PACKAGING.resolve("icon.png"), pngs.get(512));   // Linux
        writeIco(pngs);                                              // Windows
        writeIcns(pngs);                                             // macOS
    }

    // Format ICO : un en-tête, un répertoire d'images, puis les images PNG (acceptées depuis Windows Vista)
    static void writeIco(Map<Integer, byte[]> pngs) throws IOException {
        int[] sizes = {16, 32, 48, 64, 128, 256};
        int total = 0;
        for (int size : sizes) total += pngs.get(size).length;

        ByteBuffer buffer = ByteBuffer.allocate(6 + 16 * sizes.length + total).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) 0).putShort((short) 1).putShort((short) sizes.length);
        int offset = 6 + 16 * sizes.length;
        for (int size : sizes) {
            byte[] data = pngs.get(size);
            buffer.put((byte) (size == 256 ? 0 : size)).put((byte) (size == 256 ? 0 : size));
            buffer.put((byte) 0).put((byte) 0);
            buffer.putShort((short) 1).putShort((short) 32);
            buffer.putInt(data.length).putInt(offset);
            offset += data.length;
        }
        for (int size : sizes) buffer.put(pngs.get(size));
        Files.write(PACKAGING.resolve("icon.ico"), buffer.array());
    }

    // Format ICNS : "icns", la longueur totale, puis des blocs (type sur 4 lettres, longueur, image PNG)
    static void writeIcns(Map<Integer, byte[]> pngs) throws IOException {
        Object[][] entries = {
                {"icp4", 16}, {"icp5", 32}, {"icp6", 64},
                {"ic07", 128}, {"ic08", 256}, {"ic09", 512}, {"ic10", 1024},
                {"ic11", 32}, {"ic12", 64}, {"ic13", 256}, {"ic14", 512}};
        int total = 8;
        for (Object[] entry : entries) total += 8 + pngs.get((Integer) entry[1]).length;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeBytes("icns");
        out.writeInt(total);
        for (Object[] entry : entries) {
            byte[] data = pngs.get((Integer) entry[1]);
            out.writeBytes((String) entry[0]);
            out.writeInt(8 + data.length);
            out.write(data);
        }
        Files.write(PACKAGING.resolve("icon.icns"), bytes.toByteArray());
    }

    // ------------------------------------------------------------------ sons

    interface Wave {
        double value(double t);
    }

    static final Random RANDOM = new Random(3);

    static double noise() { return RANDOM.nextDouble() * 2 - 1; }

    static double square(double phase) { return (phase - Math.floor(phase)) < 0.5 ? 1 : -1; }

    static double sawtooth(double phase) { return 2 * (phase - Math.floor(phase)) - 1; }

    static double triangle(double phase) { return 4 * Math.abs(phase - Math.floor(phase + 0.5)) - 1; }

    static double decay(double t, double duration) { return Math.max(0, 1 - t / duration); }

    static void sound(String name, double duration, Wave wave) throws IOException {
        int n = (int) (duration * SAMPLE_RATE);
        byte[] data = new byte[n * 2];
        for (int i = 0; i < n; i++) {
            double t = i / (double) SAMPLE_RATE;
            double attack = Math.min(1, t / 0.004);   // évite le « clic » au début
            double v = Math.max(-1, Math.min(1, wave.value(t))) * 0.35 * attack;
            short s = (short) (v * Short.MAX_VALUE);
            data[2 * i] = (byte) (s & 0xff);
            data[2 * i + 1] = (byte) ((s >> 8) & 0xff);
        }
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        try (AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(data), format, n)) {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, RESOURCES.resolve(name).toFile());
        }
    }
}
