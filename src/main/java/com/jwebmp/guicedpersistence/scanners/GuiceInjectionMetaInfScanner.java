package com.guicedee.guicedpersistence.scanners;

import com.guicedee.guicedinjection.interfaces.IPathContentsScanner;

import java.util.HashSet;
import java.util.Set;

public class GuiceInjectionMetaInfScanner
		implements IPathContentsScanner
{
	@Override
	public Set<String> searchFor()
	{
		Set<String> strings = new HashSet<>();
		strings.add("META-INF");
		return strings;
	}
}
