package net.proselyte.webfluxsecurity.mapper;

import net.proselyte.webfluxsecurity.dto.ItemResponse;
import net.proselyte.webfluxsecurity.entity.ItemEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemResponse map (ItemEntity itemEntity);


}
