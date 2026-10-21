package br.edu.unijorge.urnacidada.config;

import br.edu.unijorge.urnacidada.repository.FirestoreVotoRepository;
import br.edu.unijorge.urnacidada.repository.MemoriaVotoRepository;
import br.edu.unijorge.urnacidada.repository.VotoRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Inicializa o Firebase a partir do arquivo de credenciais (chave da conta de serviço).
 *
 * Onde o arquivo é procurado, nesta ordem:
 * <ol>
 *   <li>caminho na variável de ambiente {@code URNA_FIREBASE_CREDENCIAIS};</li>
 *   <li>arquivo {@code serviceAccountKey.json} na pasta onde o programa é executado.</li>
 * </ol>
 * Se não encontrar (ou der erro), a urna funciona em modo offline, guardando os votos na memória.
 *
 * NUNCA envie o serviceAccountKey.json para o GitHub (ele já está no .gitignore).
 */
public final class FirebaseConfig {

    public static final String VARIAVEL_CREDENCIAIS = "URNA_FIREBASE_CREDENCIAIS";
    public static final String ARQUIVO_PADRAO = "serviceAccountKey.json";
    public static final String VARIAVEL_URNA_ID = "URNA_ID";

    private FirebaseConfig() {
    }

    public static VotoRepository criarRepositorio() {
        Path credenciais = localizarCredenciais();
        if (credenciais == null) {
            System.out.println("[Urna Cidadã] Credenciais do Firebase não encontradas ("
                    + ARQUIVO_PADRAO + "). Usando modo offline.");
            return new MemoriaVotoRepository();
        }
        try (InputStream in = new FileInputStream(credenciais.toFile())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            Firestore db = FirestoreClient.getFirestore();
            String urnaId = System.getenv().getOrDefault(VARIAVEL_URNA_ID, "urna-01");
            System.out.println("[Urna Cidadã] Firebase inicializado. Projeto: " + db.getOptions().getProjectId());
            return new FirestoreVotoRepository(db, urnaId);
        } catch (Exception e) {
            System.err.println("[Urna Cidadã] Falha ao inicializar o Firebase: " + e.getMessage()
                    + ". Usando modo offline.");
            return new MemoriaVotoRepository();
        }
    }

    private static Path localizarCredenciais() {
        String doAmbiente = System.getenv(VARIAVEL_CREDENCIAIS);
        if (doAmbiente != null && !doAmbiente.isBlank()) {
            Path p = Path.of(doAmbiente);
            if (Files.isReadable(p)) {
                return p;
            }
            System.err.println("[Urna Cidadã] Arquivo indicado em " + VARIAVEL_CREDENCIAIS + " não existe: " + p);
        }
        Path padrao = Path.of(ARQUIVO_PADRAO);
        return Files.isReadable(padrao) ? padrao : null;
    }
}
