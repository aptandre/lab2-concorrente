import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This class provides functionality to apply a mean filter to an image.
 * The mean filter is used to smooth images by averaging the pixel values
 * in a neighborhood defined by a kernel size.
 * 
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ImageMeanFilter.applyMeanFilter("input.jpg", "output.jpg", 3);
 * }
 * </pre>
 * 
 * <p>Supported image formats: JPG, PNG</p>
 * 
 * <p>Author: temmanuel@comptuacao.ufcg.edu.br</p>
 */
public class ImageMeanFilter {
    
    /**
     * Applies mean filter to an image
     * 
     * @param inputPath  Path to input image
     * @param outputPath Path to output image 
     * @param kernelSize Size of mean kernel
     * @throws IOException If there is an error reading/writing
     */
    public static void applyMeanFilter(String inputPath, String outputPath, int kernelSize, int qtThreads) throws IOException, InterruptedException {

        // Load image
        BufferedImage originalImage = ImageIO.read(new File(inputPath));
        
        // Create result image
        BufferedImage filteredImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        // AQUI
        int x = 0;
        int y = 0;
        for (int i = 0; i < qtThreads; i++) {
            Thread imageThread = new Thread(new ImageRunnable(originalImage, kernelSize, filteredImage, x, y, kernelSize), "imageThread");
            x += kernelSize;
            y += kernelSize;
            imageThread.start();
            imageThread.join();
        }
        // AQUI
        
        // Save filtered image
        ImageIO.write(filteredImage, "jpg", new File(outputPath));
    }
    
    /**
     * Calculates average colors in a pixel's neighborhood
     * 
     * @param image      Source image
     * @param centerX    X coordinate of center pixel
     * @param centerY    Y coordinate of center pixel
     * @param kernelSize Kernel size
     * @return Array with R, G, B averages
     */
    private static int[] calculateNeighborhoodAverage(BufferedImage image, int centerX, int centerY, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int pad = kernelSize / 2;
        
        // Arrays for color sums
        long redSum = 0, greenSum = 0, blueSum = 0;
        int pixelCount = 0;
        
        // Process neighborhood
        for (int dy = -pad; dy <= pad; dy++) {
            for (int dx = -pad; dx <= pad; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
                
                // Check image bounds
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    // Get pixel color
                    int rgb = image.getRGB(x, y);
                    
                    // Extract color components
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    
                    // Sum colors
                    redSum += red;
                    greenSum += green;
                    blueSum += blue;
                    pixelCount++;
                }
            }
        }
        
        // Calculate average
        return new int[] {
            (int)(redSum / pixelCount),
            (int)(greenSum / pixelCount),
            (int)(blueSum / pixelCount)
        };
    }
    
    /**
     * Main method for demonstration
     * 
     * Usage: java ImageMeanFilter <input_file>
     * 
     * Arguments:
     *   input_file - Path to the input image file to be processed
     *                Supported formats: JPG, PNG
     * 
     * Example:
     *   java ImageMeanFilter input.jpg
     * 
     * The program will generate a filtered output image named "filtered_output.jpg"
     * using a 7x7 mean filter kernel
          * @throws InterruptedException 
          */
         public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            System.err.println("Usage: java ImageMeanFilter <input_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String amountThreads = args[1];
        try {
            int qtThreads = Integer.parseInt(amountThreads);
            applyMeanFilter(inputFile, "filtered_output.jpg", 7, qtThreads);
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

    public static class ImageRunnable implements Runnable {

        private BufferedImage originalImage;
        private BufferedImage filteredImage;
        private int kernelSize;
        private int x_value;
        private int y_value;
        private int window_size;

        public ImageRunnable(BufferedImage original, int kernelSize, BufferedImage filteredImage, int x, int y, int window_size) {
            this.originalImage = original;
            this.kernelSize = kernelSize;
            this.filteredImage = filteredImage;
            this.x_value = x;
            this.y_value = y;
            this.window_size = window_size;
        }

        @Override
        public void run() {
            int width = this.originalImage.getWidth();
            int height = this.originalImage.getHeight();
            // Process each pixel
            for (int y = y_value; y < y_value + window_size; y++) {
                for (int x = x_value; x < x_value + window_size; x++) {
                    // Calculate neighborhood average
                    int[] avgColor = calculateNeighborhoodAverage(this.originalImage, x, y, this.kernelSize);
                    
                    // Set filtered pixel
                    filteredImage.setRGB(x, y, 
                        (avgColor[0] << 16) | 
                        (avgColor[1] << 8)  | 
                        avgColor[2]
                    );
                }
            }
        }
    }
}