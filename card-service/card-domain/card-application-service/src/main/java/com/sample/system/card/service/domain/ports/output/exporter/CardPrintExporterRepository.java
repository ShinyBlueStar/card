package com.sample.system.card.service.domain.ports.output.exporter;

import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.response.accessfile.result.AccessFileResult;

import java.util.List;
public interface CardPrintExporterRepository {
    AccessFileResult export(List<Card> cards);
}
