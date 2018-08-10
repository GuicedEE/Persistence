package com.jwebmp.guicedpersistence.scanners;

import com.jwebmp.guicedinjection.interfaces.IPathContentsBlacklistScanner;

import java.util.HashSet;
import java.util.Set;

public class GuiceInjectionMetaInfScannerExclusions
		implements IPathContentsBlacklistScanner
{
	@Override
	public Set<String> searchFor()
	{
		Set<String> strings = new HashSet<>();
		strings.add("META-INF/resources");
		strings.add("META-INF/services");
		return strings;
	}
}
