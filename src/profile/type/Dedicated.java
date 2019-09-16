/*
 * This code is part of the ThornSec project.
 *
 * To learn more, please head to its GitHub repo: @privacyint
 *
 * Pull requests encouraged.
 */
package profile.type;

import java.util.ArrayList;
import java.util.Collection;

import core.exception.runtime.InvalidServerModelException;
import core.iface.IUnit;
import core.model.network.NetworkModel;
import core.profile.AStructuredProfile;

/**
 * This is a dedicated server on your network. This is something ThornSec needs
 * to know about, but shouldn't attempt to configure
 */
public class Dedicated extends AStructuredProfile {

	public Dedicated(String label, NetworkModel networkModel) throws InvalidServerModelException {
		super(label, networkModel);
	}

	@Override
	public Collection<IUnit> getPersistentFirewall() throws InvalidServerModelException {
		final Collection<IUnit> units = new ArrayList<>();

		getNetworkModel().getServerModel(getLabel()).addEgress("cdn.debian.net:80");
		getNetworkModel().getServerModel(getLabel()).addEgress("security-cdn.debian.org:80");
		getNetworkModel().getServerModel(getLabel()).addEgress("prod.debian.map.fastly.net:80");
		getNetworkModel().getServerModel(getLabel()).addEgress("download.virtualbox.org:80");
		getNetworkModel().getServerModel(getLabel()).addEgress("cdn.debian.net:443");
		getNetworkModel().getServerModel(getLabel()).addEgress("security-cdn.debian.org:443");
		getNetworkModel().getServerModel(getLabel()).addEgress("prod.debian.map.fastly.net:443");
		getNetworkModel().getServerModel(getLabel()).addEgress("download.virtualbox.org:443");

		return units;
	}
}
