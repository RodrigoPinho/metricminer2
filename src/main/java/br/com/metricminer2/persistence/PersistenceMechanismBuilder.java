/**
 * Copyright 2014 Maurício Aniche

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.metricminer2.persistence;

import br.com.metricminer2.MMOptions;
import br.com.metricminer2.persistence.csv.CSVFile;
import br.com.metricminer2.persistence.csv.GoogleStorage;
import br.com.metricminer2.persistence.csv.SysOut;

public class PersistenceMechanismBuilder {

	public PersistenceMechanism from(MMOptions opts) {
		if (opts.hasCloudStorage()) {
			return new GoogleStorage(opts.getCloudStorage());
		}
		else if(opts.hasCsv()) {
			return new CSVFile(opts.getCsv());
		} else if(opts.isSysOut()) {
			return new SysOut();
		}

		throw new PersistenceMechanismException("Persistence mechanism not found.");
	}
}
