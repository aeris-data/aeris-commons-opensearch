package fr.aeris.commons.model.elements;

import javax.xml.namespace.QName;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.ExtensibleElementWrapper;

public class Group extends ExtensibleElementWrapper {

	public Group(Factory factory, QName qname) {
		super(factory, qname);
	}

}
