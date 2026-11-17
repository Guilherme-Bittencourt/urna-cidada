package br.edu.unijorge.urnacidada.repository;

import br.edu.unijorge.urnacidada.model.Voto;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Grava cada voto como um documento na coleção "votos" do Cloud Firestore.
 * Precisa do arquivo serviceAccountKey.json na pasta do projeto (veja o README).
 */
public class FirestoreVotoRepository implements VotoRepository {

    private final Firestore db;

    public FirestoreVotoRepository(String arquivoCredenciais) throws IOException {
        try (FileInputStream credenciais = new FileInputStream(arquivoCredenciais)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credenciais))
                    .build();
            FirebaseApp.initializeApp(options);
        }
        db = FirestoreClient.getFirestore();
    }

    @Override
    public void salvar(Voto voto) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("cargo", voto.getCargo());
        dados.put("tipo", voto.getTipo().name());
        dados.put("numero", voto.getNumero());
        dados.put("descricao", voto.getDescricao());
        dados.put("dataHora", FieldValue.serverTimestamp());
        try {
            db.collection("votos").add(dados).get(); // espera o Firebase confirmar
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível gravar o voto no Firebase", e);
        }
    }
}
