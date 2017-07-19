/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.sli.aai.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openecomp.aai.inventory.v10.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "genericVnf",
    "pserver",
    "complex"
})
@XmlRootElement(name = "results")
public class Results {

	@XmlElement(name = "generic-vnf")
	private GenericVnf genericVnf;

	@XmlElement(name = "complex")
	private Complex complex;

	@XmlElement(name = "pserver")
	private Pserver pserver;


	@XmlElement(name = "generic-vnf")
    public GenericVnf getGenericVnf ()
    {
        return genericVnf;
    }
	@XmlElement(name = "generic-vnf")
    public void setGenericVnf (GenericVnf genericVnf)
    {
        this.genericVnf = genericVnf;
    }

    @Override
    public String toString()
    {
        return " [generic-vnf = "+genericVnf+"]";
    }
    @XmlElement(name = "complex")
	public Complex getComplex() {
		return complex;
	}
	@XmlElement(name = "complex")
	public void setComplex(Complex complex) {
		this.complex = complex;
	}
	@XmlElement(name = "pserver")
	public Pserver getPserver() {
		return pserver;
	}
	@XmlElement(name = "pserver")
	public void setPserver(Pserver pserver) {
		this.pserver = pserver;
	}
}
