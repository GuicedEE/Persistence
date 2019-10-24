package com.guicedee.guicedpersistence.scanners;

import com.guicedee.guicedinjection.interfaces.IPathContentsBlacklistScanner;

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
		strings.add("META-INF/maven");
		return strings;
	}
}
