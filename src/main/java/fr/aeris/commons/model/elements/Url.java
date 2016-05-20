package fr.aeris.commons.model.elements;

import javax.xml.namespace.QName;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.ExtensibleElementWrapper;

public class Url extends ExtensibleElementWrapper {

	public Url(Factory factory, QName qname) {
		super(factory, qname);
	}

}