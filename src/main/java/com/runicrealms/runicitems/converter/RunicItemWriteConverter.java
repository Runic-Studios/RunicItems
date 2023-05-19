package com.runicrealms.runicitems.converter;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.RunicItemBook;
import com.runicrealms.runicitems.item.RunicItemDynamic;
import com.runicrealms.runicitems.item.RunicItemGem;
import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class RunicItemWriteConverter implements Converter<RunicItem, Document> {

    public RunicItemWriteConverter() {
        RunicCore.getConverterAPI().addDataConverter(this);
    }

    @Override
    public Document convert(@NotNull RunicItem source) {
        Document document = new Document();
        if (source instanceof RunicItemArmor) {
            return source.writeToDocument(source, document);
        } else if (source instanceof RunicItemArtifact) {
            return source.writeToDocument(source, document);
        } else if (source instanceof RunicItemBook) {
            return source.writeToDocument(source, document);
        } else if (source instanceof RunicItemDynamic) {
            return source.writeToDocument(source, document);
        } else if (source instanceof RunicItemGem) {
            return source.writeToDocument(source, document);
        } else if (source instanceof RunicItemGeneric) {
            return source.writeToDocument(source, document);
        } else if (source instanceof RunicItemOffhand) {
            return source.writeToDocument(source, document);
        } else if (source instanceof RunicItemWeapon) {
            return source.writeToDocument(source, document);
        }
        return document;
    }

}
