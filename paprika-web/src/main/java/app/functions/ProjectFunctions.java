package app.functions;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;

/**
 * ProjectsFunctions is a utils class linked to Project class but use neo4j
 * @author guillaume
 *
 */
public class ProjectFunctions extends Functions {


	
	/**
	 * Return the id of the project of the user
	 * 
	 * @param email the email of the user
	 * @param project the name of the project
	 * @return the id of the project, if he exist, else -1
	 */
	
	public long receiveIDOfProject(String email, String project) {
		LowNode nodeUser = new LowNode(PaprikaKeyWords.LABELUSER);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);

		LowNode nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
		nodeApp.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, project);

		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run(graph.matchSee(nodeUser, nodeApp, PaprikaKeyWords.REL_USER_PROJECT));
			tx.success();
		}
		return this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
	
	}



	/**
	 * Add 1 to a property on the User of the project
	 * @param lowNode 
	 * @param attribute 
	 * @param value 
	 * @param tx 
	 * @return increment the number of Project on the user.
	 */
	public String increment(LowNode lowNode, String attribute, long value, Transaction tx) {

		long size = value;
		size += 1;
		LowNode nodePut = new LowNode(PaprikaKeyWords.LABELUSER);
		nodePut.addParameter(attribute, size);

		tx.run(graph.set(lowNode, nodePut));
		return Long.toString(size);
	}

	/**
	 * Add a new Project on the User node.
	 * 
	 * @param email email of the User.
	 * @param project name of the project
	 * @return the id of the new Project
	 */
	public long writeProjectOnUser(String email, String project) {

		StatementResult result;
		Record record;
		Node node;
		long id=-1;
		LowNode nodeUser = new LowNode(PaprikaKeyWords.LABELUSER);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);

		// Incrémente le nombre d'projects dans l'utilisateur:
		try (Transaction tx = this.session.beginTransaction()) {

			result = tx.run(graph.matchSee(nodeUser));
			record = result.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
			PaprikaWebMain.LOGGER.trace(node.get(PaprikaKeyWords.ATTRIBUTE_NB_APP));
			LowNode nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
			nodeApp.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, project);
			nodeApp.addParameter(PaprikaKeyWords.ATTRIBUTE_NB_VERSION, 0);
			String incr = this.increment(nodeUser, PaprikaKeyWords.ATTRIBUTE_NB_APP,
					node.get(PaprikaKeyWords.ATTRIBUTE_NB_APP).asLong(), tx);
			nodeApp.addParameter(PaprikaKeyWords.ORDER, incr);

			// Créer une project node:
			result = tx.run(graph.create(nodeApp));
			id = this.graph.getID(result, PaprikaKeyWords.NAMELABEL);
			// Récupère son id:
			nodeApp.setId(id);

			tx.run(graph.relation(nodeUser, nodeApp, PaprikaKeyWords.REL_USER_PROJECT));
			tx.success();
		}
		return id;
	}

	/**
	 * Return the name of the id of a unknow node if the node exist and than a name exist.
	 * @param idnode
	 * @return a name.
	 */
	public String receiveOf(long idnode) {
		return PaprikaFacade.getInstance().getParameter(idnode, PaprikaKeyWords.NAMEATTRIBUTE);
	}
}
