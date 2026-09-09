package de.jlo.talendcomp.sap.proxyservice;

import java.text.SimpleDateFormat;
import java.util.List;

import io.prometheus.client.Collector;
import io.prometheus.client.Info;

public class ServiceInfoExports extends Collector {

	@Override
	public List<MetricFamilySamples> collect() {
		final Info i = Info.build().name("service").help("Service Info").create();
        i.info(
            "startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
            "version", Main.version
        );
        return i.collect();
	}

}
