package br.edu.unijorge.urnacidada.repository;

import br.edu.unijorge.urnacidada.model.RegistroVoto;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Persistência dos votos no Firebase Cloud Firestore.
 *
 * Estrutura no banco (coleção {@code votos}, um documento por voto, ID automático):
 * <pre>
 * votos/{idAutomatico}
 *   cargo:        "Vereador"
 *   tipo:         "NOMINAL" | "LEGENDA" | "BRANCO" | "NULO"
 *   numero:       "42010"
 *   descricao:    "Nina Biblioteca"
 *   siglaPartido: "PLIV"        (null em branco/nulo)
 *   urnaId:       "urna-lab-01"
 *   dataHora:     timestamp do servidor
 * </pre>
 * Nenhum dado do eleitor é gravado (sigilo do voto).
 */
public class FirestoreVotoRepository implements VotoRepository {

    public static final String COLECAO = "votos";
    private static final int TAMANHO_LOTE = 400; // limite do Firestore é 500 operações por lote

    private final Firestore db;
    private final String urnaId;

    public FirestoreVotoRepository(Firestore db, String urnaId) {
        this.db = Objects.requireNonNull(db);
        this.urnaId = Objects.requireNonNull(urnaId);
    }

    private CollectionReference colecao() {
        return db.collection(COLECAO);
    }

    @Override
    public void salvar(RegistroVoto voto) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("cargo", voto.cargo());
        dados.put("tipo", voto.tipo().name());
        dados.put("numero", voto.numero());
        dados.put("descricao", voto.descricao());
        dados.put("siglaPartido", voto.siglaPartido());
        dados.put("urnaId", urnaId);
        dados.put("dataHora", FieldValue.serverTimestamp());
        try {
            colecao().add(dados).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositorioException("Gravação interrompida", e);
        } catch (ExecutionException e) {
            throw new RepositorioException("Não foi possível gravar o voto no Firebase", e.getCause());
        }
    }

    @Override
    public List<RegistroVoto> listarTodos() {
        try {
            QuerySnapshot snapshot = colecao().get().get();
            List<RegistroVoto> votos = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                String tipo = doc.getString("tipo");
                if (doc.getString("cargo") == null || tipo == null) {
                    continue; // documento incompleto: ignora
                }
                votos.add(new RegistroVoto(
                        doc.getString("cargo"),
                        TipoVoto.valueOf(tipo),
                        Objects.requireNonNullElse(doc.getString("numero"), ""),
                        Objects.requireNonNullElse(doc.getString("descricao"), ""),
                        doc.getString("siglaPartido")));
            }
            return votos;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositorioException("Leitura interrompida", e);
        } catch (ExecutionException e) {
            throw new RepositorioException("Não foi possível ler os votos do Firebase", e.getCause());
        }
    }

    @Override
    public void apagarTodos() {
        try {
            List<QueryDocumentSnapshot> docs;
            do {
                docs = colecao().limit(TAMANHO_LOTE).get().get().getDocuments();
                if (docs.isEmpty()) {
                    break;
                }
                WriteBatch lote = db.batch();
                for (QueryDocumentSnapshot doc : docs) {
                    lote.delete(doc.getReference());
                }
                lote.commit().get();
            } while (docs.size() == TAMANHO_LOTE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositorioException("Operação interrompida", e);
        } catch (ExecutionException e) {
            throw new RepositorioException("Não foi possível apagar os votos no Firebase", e.getCause());
        }
    }

    @Override
    public String getDescricao() {
        return "Conectado ao Firebase (Cloud Firestore) – " + db.getOptions().getProjectId();
    }
}
