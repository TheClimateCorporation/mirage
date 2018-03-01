package com.climate.mirage.cache;

import com.climate.mirage.processors.BitmapProcessor;
import com.climate.mirage.requests.MirageRequest;

import java.util.List;

public class SimpleKeyMaker implements KeyMaker {

	@Override
	public String getSourceKey(MirageRequest request) {
		StringBuilder b = new StringBuilder();
		b.append(request.provider().id().hashCode());
		return Integer.toHexString(b.toString().hashCode());
	}

	@Override
	public String getResultKey(MirageRequest request) {
		StringBuilder b = new StringBuilder();
        b.append(request.provider().id().hashCode());
//        b.append(request.uri().toString().hashCode());

        if (request.isInSampleSizeDynamic()) {
            b.append("_size");
            b.append(request.getResizeTargetDimen());
            b.append("-under");
            b.append(request.isResizeSampleUndershoot());
        } else if (request.options() != null) {
			b.append("_");
			b.append(request.options().inSampleSize);
        }

		List<BitmapProcessor> processors = request.getProcessors();
		if (processors != null) {
			for (int i=0; i<processors.size(); i++) {
				b.append("_");
				b.append(processors.get(i).getId());
			}
		}

		return Integer.toHexString(b.toString().hashCode());
	}
}
