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

package paprikaana.utils.neo4j.query;

import java.io.File;
import java.io.IOException;

import org.neo4j.cypher.CypherException;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import paprikaana.utils.neo4j.QueryEngineBolt;

/**
 * Created by Geoffrey Hecht on 17/08/15.
 */
public abstract class FuzzyQuery extends Query {
    protected String fclFile;

    public FuzzyQuery(QueryEngineBolt queryEngine) {
        super(queryEngine);
    }

    public abstract void executeFuzzy(boolean details) throws CypherException, IOException;
    
    
    protected FunctionBlock fuzzyFunctionBlock(){
        File fcf = new File(fclFile);
        //We look if the file is in a directory otherwise we look inside the jar
        FIS fis;
        if(fcf.exists() && !fcf.isDirectory()){
            fis = FIS.load(fclFile, false);
        }else{
            fis = FIS.load(getClass().getResourceAsStream(fclFile),false);
        }
        return fis.getFunctionBlock(null);
    }
}
