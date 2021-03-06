package spoon.main;
/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import tandoori.neo4jBolt.Graph;
import tandoori.neo4jBolt.LowNode;
import tandoori.neo4jBolt.QueryEngineBolt;
import spoon.main.processor.AnnotateProcessor;

import java.io.IOException;
import java.util.*;

/**
 * Modified for capture all code smells.
 * 
 * Add on these two methods overrideds, a hashset who take all code smells
 * founded on put them on a Map of Set of the annotateProcess
 * 
 * @author guillaume
 *
 */
@SuppressWarnings("javadoc")
public class QueryEngineBoltModified extends QueryEngineBolt {

	public QueryEngineBoltModified(long keyApp) {
		super(keyApp);
	}

	@Override
	public void resultToCSV(StatementResult result, String nameQuery) throws IOException {
		StatementResult result_query;
		try (Transaction tx = this.session.beginTransaction()) {

			Value val;

			// Je crée le noeud Query data, exemple: Blob
			LowNode nodeQueryData = new LowNode(nameQuery);
			nodeQueryData.addParameter(Graph.APPKEY, this.keyApp);
			result_query = tx.run(this.graph.create(nodeQueryData));
			Record record = result_query.next();
			Node node = record.get(Graph.NAMELABEL).asNode();

			// NodefastRel ne contient que le label et l'id, pour effectuer des
			// matchs plus rapidement, nodeQueryData servira à mettre à jour par
			// 1 set, le query
			LowNode nodeFastRel = new LowNode(nameQuery);
			nodeFastRel.setId(node.id());

			/*
			 * Je prends le noeud Query du pathnane donné qui a dû être crée
			 * précedemment. et ensuite je le lie à Blob, comme je le ferais à
			 * tous les autres noeuds QueryData
			 */
			LowNode nodeQuery = new LowNode(Graph.LABELQUERY);
			nodeQuery.addParameter(Graph.APPKEY, this.keyApp);
			tx.run(this.graph.relation(nodeQuery, nodeFastRel, Graph.REL_CODESMELLS_CAS));

			//
			Set<String> hashset = new HashSet<String>();

			//

			LowNode nodeClass;
			long number = 0;
			while (result.hasNext()) {

				Record row = result.next();
				val = row.get("nod");
				if (val != null && !val.isNull()) {
					// si la colonne est de nom node, alors on ajoute une
					// relation au node en question.
					node = val.asNode();
					nodeClass = new LowNode(node.labels().iterator().next());
					nodeClass.setId(node.id());
					LowNode nodeRel = new LowNode(Graph.REL_CAS_CODE);
					val = row.get(QueryEngineBolt.FUZZY);
					if (val != null && !val.isNull()) {
						nodeRel.addParameter(QueryEngineBolt.FUZZY, val.asObject());
					}
					number++;
					tx.run(this.graph.relation(nodeFastRel, nodeClass, nodeRel));
				}
				val = row.get("full_name");
				if (val != null && !val.isNull()) {
					String name = val.asString();
					hashset.add(name);
				}

			}
			System.out.println("***");
			System.out.println(nameQuery);
			System.out.println(hashset.toString());
			System.out.println("***");
			AnnotateProcessor.codesmells.put(nameQuery, hashset);

			nodeQueryData.addParameter("number", number);
			tx.run(this.graph.set(nodeFastRel, nodeQueryData));

			tx.success();
		}
	}

	@Override
	public void resultToCSV(List<Map> rows, String nameQuery) throws IOException {
		StatementResult result_query;
		try (Transaction tx = this.session.beginTransaction()) {

			Object val;

			// Je crée le noeud Query data, exemple: Blob
			LowNode nodeQueryData = new LowNode(nameQuery);
			nodeQueryData.addParameter(Graph.APPKEY, this.keyApp);
			result_query = tx.run(this.graph.create(nodeQueryData));
			Record record = result_query.next();
			Node node = record.get(Graph.NAMELABEL).asNode();

			// NodefastRel ne contient que le label et l'id, pour effectuer des
			// matchs plus rapidement, nodeQueryData servira à mettre à jour par
			// 1 set, le query
			LowNode nodeFastRel = new LowNode(nameQuery);
			nodeFastRel.setId(node.id());

			/*
			 * Je prends le noeud Query du pathnane donné qui a dû être crée
			 * précedemment. et ensuite je le lie à Blob, comme je le ferais à
			 * tous les autres noeuds QueryData
			 */
			LowNode nodeQuery = new LowNode(Graph.LABELQUERY);
			nodeQuery.addParameter(Graph.APPKEY, this.keyApp);
			tx.run(this.graph.relation(nodeQuery, nodeFastRel, Graph.REL_CODESMELLS_CAS));

			//
			Set<String> hashset = new HashSet<String>();

			//

			LowNode nodeClass;
			long number = 0;
			for (Map<String, Object> row : rows) {

				System.out.println(row.keySet().toString());
				val = row.get("nod");
				if (val != null) {
					// si la colonne est de nom node, alors on ajoute une
					// relation au node en question.
					node = (Node) val;
					nodeClass = new LowNode(node.labels().iterator().next());
					nodeClass.setId(node.id());
					LowNode nodeRel = new LowNode(Graph.REL_CAS_CODE);
					val = row.get(QueryEngineBolt.FUZZY);
					if (val != null) {
						nodeRel.addParameter(QueryEngineBolt.FUZZY, val);
					}
					number++;
					tx.run(this.graph.relation(nodeFastRel, nodeClass, nodeRel));
				}
				val = row.get("full_name");
				if (val != null) {
					String name = (String) val;
					hashset.add(name);
				}

			}
			AnnotateProcessor.codesmells.put(nameQuery, hashset);

			System.out.println("***");
			System.out.println(nameQuery);
			System.out.println(hashset.toString());
			System.out.println("***");

			nodeQueryData.addParameter("number", number);
			tx.run(this.graph.set(nodeFastRel, nodeQueryData));

			tx.success();
		}
	}

}