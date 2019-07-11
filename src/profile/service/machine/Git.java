/*
 * This code is part of the ThornSec project.
 * 
 * To learn more, please head to its GitHub repo: @privacyint
 * 
 * Pull requests encouraged.
 */
package profile.service.machine;

import java.util.HashSet;
import java.util.Set;

import core.iface.IUnit;
import core.model.network.NetworkModel;

import core.profile.AStructuredProfile;

import profile.stack.Nginx;

import core.unit.SimpleUnit;
import core.unit.fs.FileEditUnit;
import core.unit.fs.FileUnit;
import core.unit.pkg.InstalledUnit;
import core.unit.pkg.RunningUnit;

import core.exception.data.InvalidPortException;
import core.exception.data.machine.InvalidServerException;
import core.exception.runtime.InvalidServerModelException;

/**
 * This profile is supposed to create and configure a Git server
 */
public class Git extends AStructuredProfile {

	private Nginx webserver;
	
	public Git(String label, NetworkModel networkModel) {
		super("git", networkModel);
		
		this.webserver = new Nginx(getLabel(), networkModel);
	}

	protected Set<IUnit> getInstalled()
	throws InvalidServerModelException {
		Set<IUnit> units = new HashSet<IUnit>();
		
		units.add(new InstalledUnit("java", "proceed", "default-jre-headless"));
				
		units.add(new InstalledUnit("scm_server", "scm_manager_pgp", "scm-server"));
		
		units.addAll(webserver.getInstalled());
		
		return units;
	}
	
	protected Set<IUnit> getPersistentConfig()
	throws InvalidServerException, InvalidServerModelException {
		Set<IUnit> units =  new HashSet<IUnit>();
				
		units.addAll(networkModel.getServerModel(getLabel()).getBindFsModel().addDataBindPoint("scm", "proceed", "scm", "scm", "0750"));
		
		units.add(new FileEditUnit("scm_server_home", "scm_data_mounted", "export SCM_HOME=/var/lib/scm", "export SCM_HOME=/media/data/scm", "/etc/default/scm-server",
				"Couldn't change scm-manager's data directory.  Its data will be stored in the VM only."));
		
		units.add(new RunningUnit("scm_server", "scm-server", "scm-server"));
		
		FileUnit nginxConf = new FileUnit("scm_manager_default_nginx", "scm_server_installed", Nginx.CONF_D_DIRECTORY + "default.conf");
		
		nginxConf.appendLine("server {");
		nginxConf.appendLine("    listen 80;");
		nginxConf.appendLine("    server_name _;");
		nginxConf.appendLine("");
		nginxConf.appendLine("    location / {");
		nginxConf.appendLine("        proxy_pass          http://localhost:8080/scm/;");
		nginxConf.appendLine("        proxy_set_header    Host \\$host;");
		nginxConf.appendLine("        proxy_set_header    X-Real-IP \\$remote_addr;");
		nginxConf.appendLine("        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504 http_404;");
		nginxConf.appendLine("        proxy_redirect      off;");
		nginxConf.appendLine("        proxy_cache_valid   200 120m;");
		nginxConf.appendLine("        proxy_buffering     on;");
		nginxConf.appendLine("        proxy_set_header    Accept-Encoding \"\";");
		nginxConf.appendLine("    }");
		nginxConf.appendLine("}");
		
		webserver.addLiveConfig(nginxConf);
		
		units.addAll(webserver.getPersistentConfig());
		
		FileUnit scmService = new FileUnit("scm_service", "scm_server_installed", "/etc/systemd/system/scm.service");
		units.add(scmService);
		
		scmService.appendLine("[Unit]");
		scmService.appendLine("Description=scm-manager");
		scmService.appendLine("After=network.target auditd.service");
		scmService.appendLine("");
		scmService.appendLine("[Service]");
		scmService.appendLine("ExecStart=/etc/init.d/scm-server start");
		scmService.appendLine("ExecStop=/etc/init.d/scm-server stop");
		scmService.appendLine("Type=forking");
		scmService.appendLine("Restart=always");
		scmService.appendLine("");
		scmService.appendLine("[Install]");
		scmService.appendLine("WantedBy=default.target");

		units.add(new SimpleUnit("scm_service_enabled", "scm_service",
				"sudo systemctl enable scm.service",
				"systemctl status scm.service 2>&1", "Unit scm.service could not be found.", "fail"));
		
		return units;
	}

	protected Set<IUnit> getLiveConfig()
	throws InvalidServerModelException {
		Set<IUnit> units = new HashSet<IUnit>();
		
		units.addAll(webserver.getLiveConfig());
		
		return units;
	}
	
	public Set<IUnit> getPersistentFirewall()
	throws InvalidServerModelException, InvalidPortException {
		Set<IUnit> units = new HashSet<IUnit>();
		
		networkModel.getServerModel(getLabel()).getAptSourcesModel().addAptSource("scm_manager", "deb http://maven.scm-manager.org/nexus/content/repositories/releases ./", "keyserver.ubuntu.com", "D742B261");
		networkModel.getServerModel(getLabel()).addEgress("maven.scm-manager.org");

		units.addAll(webserver.getPersistentFirewall());

		return units;
	}

}