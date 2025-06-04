import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class test2 extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new test2().start());
    }

    public void start() {
        final String zipPath = "Client.zip";
        final String clientFolder = "./Client";

        // Check if client files already exist
        boolean filesExist = new File(zipPath).exists() &&
                             new File(clientFolder).exists() &&
                             new File(clientFolder).isDirectory() &&
                             new File(clientFolder).list().length > 0;

        if (filesExist) {
            System.out.println("Client files already present. Launching client...");
            launchClient();
            dispose();
            return;
        }

        // Files missing, need to download
        setSize(400, 80);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        final JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Downloading...");

        // Start download in background thread
        new Thread(new Runnable() {
            public void run() {
                // Show progress bar after thread starts
                SwingUtilities.invokeLater(() -> {
                    add(progressBar);
                    validate();
                });

                try {
                    // Download file
                    downloadFile("https://github.com/buyaka12/testing/raw/refs/heads/main/Client.zip", zipPath, progressBar);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Remove progress bar and proceed
                SwingUtilities.invokeLater(() -> {
                    remove(progressBar);
                    validate();
                    dispose(); // close window
                });

                // Unzip and launch client after download
                try {
                    unzip(zipPath, clientFolder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Launch the client
                launchClient();
            }
        }).start();
    }

    private void downloadFile(String urlStr, String savePath, JProgressBar pb) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int length = conn.getContentLength();

        try (InputStream in = conn.getInputStream(); OutputStream out = new FileOutputStream(savePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead, totalRead = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                int progress = (int) ((totalRead * 100L) / length);
                SwingUtilities.invokeLater(() -> pb.setValue(progress));
            }
        }
        conn.disconnect();
    }

    private void unzip(String zipFilePath, String destDir) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String filePath = destDir + File.separator + entry.getName();
                if (entry.isDirectory()) {
                    new File(filePath).mkdirs();
                } else {
                    new File(filePath).getParentFile().mkdirs();
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = zin.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    }
                }
                zin.closeEntry();
            }
        }
        System.out.println("Unzip complete");
    }

    private void launchClient() {
        System.out.println("Launching client...");
        // Replace with your actual client launch command
        String targetDir = "./Client";
        String[] command = {"cmd", "/c", "java", "client"}; // adjust as needed
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(targetDir));
        try {
            Process p = pb.start();
            p.waitFor();
            System.out.println("Client process finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}