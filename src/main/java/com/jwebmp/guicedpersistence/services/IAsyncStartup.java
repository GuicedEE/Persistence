package com.jwebmp.guicedpersistence.services;

import javax.validation.constraints.NotNull;
import java.util.Comparator;

/**
 * IAsync Startup Service.
 * Starts up services in a seperate executor service outside of the booting thread
 */
public interface IAsyncStartup
		extends Comparable<IAsyncStartup>, Comparator<IAsyncStartup>
{
	@Override
	default int compare(IAsyncStartup o1, IAsyncStartup o2)
	{
		if (o1 == null || o2 == null)
		{
			return -1;
		}
		return o1.sortOrder()
		         .compareTo(o2.sortOrder());
	}

	default Integer sortOrder()
	{
		return 100;
	}

	@Override
	default int compareTo(@NotNull IAsyncStartup o)
	{
		int sort = sortOrder().compareTo(o.sortOrder());
		if (sort == 0)
		{
			return -1;
		}
		return sort;
	}

}
