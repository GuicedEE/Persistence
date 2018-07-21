package com.jwebmp.guicedpersistence.scanners;

import com.jwebmp.guicedinjection.interfaces.IPackageContentsScanner;

import java.util.HashSet;
import java.util.Set;

public class GuiceInjectionMetaInfScanner
		implements IPackageContentsScanner
{
	@Override
	public Set<String> searchFor()
	{
		Set<String> strings = new HashSet<>();
		strings.add("META-INF");
		strings.add("com.jwebmp.guicedpersistence");
		return strings;
	}
}
