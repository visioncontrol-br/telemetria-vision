package visioncontrol.mensageria.telemetria.infrastructure.interfaces;

import visioncontrol.mensageria.telemetria.infrastructure.entity.PosicoesEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;

public interface TelemetryStorageService {

    void saveTelemetria(TelemetriaEntity entity);
    void savePosicao(PosicoesEntity entity);
    void saveRawPayload(String json);

}
