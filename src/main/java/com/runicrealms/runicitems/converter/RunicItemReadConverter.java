package com.runicrealms.runicitems.converter;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicitems.DupeManager;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class RunicItemReadConverter implements Converter<Document, RunicItem> {

    public RunicItemReadConverter() {
        RunicCore.getConverterAPI().addDataConverter(this);
    }

    @Override
    public RunicItem convert(@NotNull Document source) {
        return ItemLoader.loadFromDocument(source, DupeManager.getNextItemId());
    }

}
