package paprikaana.functions;

import org.neo4j.driver.v1.Transaction;

import paprikaana.utils.neo4j.LowNode;
import paprikaana.utils.neo4j.PaprikaKeyWords;


public class VersionFunctions extends Functions {



	

	/**
	 * Créer le noeud Query, avec sa relation au noeud de la Version, pour cela,
	 * il faut le noeud de la version et la clé de Query, pour pouvoir relier
	 * ensuite, les données du Query à ce noeud Query
	 * 
	 * @param nodeVer
	 * @param keyQuery
	 */
	public void writeQueryOnVersion(LowNode nodeVer, String keyQuery) {

		LowNode nodeResult = new LowNode(PaprikaKeyWords.LABELQUERY);
		// J'utilise nameattribute car le query a pour nom sa clé, pour moi.
		nodeResult.addParameter(PaprikaKeyWords.APPKEY, keyQuery);
		try (Transaction tx = this.session.beginTransaction()) {

			tx.run(this.graph.create(nodeResult));

			tx.run(this.graph.relation(nodeVer, nodeResult, PaprikaKeyWords.REL_VERSION_CODESMELLS));
			tx.success();
		}
	}

	/**
	 * Ecrit la relation de l'analyse de paprika de la version. En y insérant
	 * aussi la clé au passage. (Pour supprimer, dans le futur)
	 * 
	 * @param nodeVer
	 */
	public void writeAnalyzeOnVersion(LowNode nodeVer, long idApp) {

		LowNode nodeResult = new LowNode(PaprikaKeyWords.LABELAPP);
		nodeResult.setId(idApp);
		try (Transaction tx = this.session.beginTransaction()) {

		tx.run(graph.relation(nodeVer, nodeResult, PaprikaKeyWords.REL_VERSION_CODE));
		tx.success();
		}
	}

}
