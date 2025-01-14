package com.mate.band.domain.band.service;

import com.mate.band.domain.band.dto.BandMemberDTO;
import com.mate.band.domain.band.dto.BandRecruitInfoResponseDTO;
import com.mate.band.domain.band.dto.RegisterBandProfileRequestDTO;
import com.mate.band.domain.band.entity.BandEntity;
import com.mate.band.domain.band.entity.BandMemberEntity;
import com.mate.band.domain.band.entity.BandRecruitInfoEntity;
import com.mate.band.domain.band.repository.BandMemberEntityRepository;
import com.mate.band.domain.band.repository.BandRecruitInfoRepository;
import com.mate.band.domain.band.repository.BandRepository;
import com.mate.band.domain.metadata.constants.MappingType;
import com.mate.band.domain.metadata.constants.MusicGenre;
import com.mate.band.domain.metadata.constants.Position;
import com.mate.band.domain.metadata.dto.DistrictDataDTO;
import com.mate.band.domain.metadata.dto.ProfileMetaDataDTO;
import com.mate.band.domain.metadata.entity.DistrictEntity;
import com.mate.band.domain.metadata.entity.DistrictMappingEntity;
import com.mate.band.domain.metadata.entity.MusicGenreMappingEntity;
import com.mate.band.domain.metadata.entity.PositionMappingEntity;
import com.mate.band.domain.metadata.repository.*;
import com.mate.band.domain.user.entity.UserEntity;
import com.mate.band.domain.user.repository.UserRepository;
import com.mate.band.global.exception.BusinessException;
import com.mate.band.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BandService {

    private final BandRepository bandRepository;
    private final UserRepository userRepository;
    private final BandMemberEntityRepository bandMemberEntityRepository;
    private final BandRecruitInfoRepository bandRecruitInfoRepository;
    private final DistrictRepository districtRepository;
    private final PositionMappingRepository positionMappingRepository;
    private final MusicGenreMappingRepository musicGenreMappingRepository;
    private final DistrictMappingRepository districtMappingRepository;

    public Boolean checkBandName(String bandName) {
        return bandRepository.findByBandName(bandName).isPresent();
    }

    @Transactional
    public Page<BandRecruitInfoResponseDTO> getBandRecruitInfoList(Pageable pageable) {
        Page<BandEntity> recruitingBandList = bandRepository.findRecruitingBandList(pageable);
        Page<BandRecruitInfoResponseDTO> bandRecruitInfoPage = recruitingBandList.map(recruitingBand -> {
            // 음악 장르 데이터
            List<ProfileMetaDataDTO> musicGenres =
                    recruitingBand.getMusicGenres().stream().map(MusicGenreMappingEntity::getGenre).toList()
                            .stream().map(musicGenre -> ProfileMetaDataDTO.builder().key(musicGenre.getkey()).value(musicGenre.getValue()).build())
                            .toList();

            // 모집 포지션 데이터
            List<ProfileMetaDataDTO> positions =
                    recruitingBand.getRecruitingPositions().stream().map(PositionMappingEntity::getPosition).toList()
                            .stream().map(position -> ProfileMetaDataDTO.builder().key(position.getkey()).value(position.getValue()).build())
                            .toList();

            // 합주 지역 데이터
            List<DistrictDataDTO> districts =
                    recruitingBand.getDistricts().stream().map(districtMappingEntity ->
                            DistrictDataDTO.builder()
                                    .districtId(districtMappingEntity.getDistrict().getId())
                                    .districtName(districtMappingEntity.getDistrict().getDistrictName())
                                    .build()
                    ).toList();

            return BandRecruitInfoResponseDTO.builder()
                    .bandId(recruitingBand.getId())
                    .bandName(recruitingBand.getBandName())
                    .recruitTitle(recruitingBand.getBandRecruitInfoEntity().getTitle())
                    .genres(musicGenres)
                    .positions(positions)
                    .districts(districts)
                    .build();
        });
        return bandRecruitInfoPage;

//        for (BandEntity recruitingBand : recruitingBandList) {
//
            // 음악 장르 데이터
//            List<ProfileMetaDataDTO> musicGenres =
//                    recruitingBand.getMusicGenres().stream().map(MusicGenreMappingEntity::getGenre).toList()
//                            .stream().map(musicGenre -> ProfileMetaDataDTO.builder().key(musicGenre.getkey()).value(musicGenre.getValue()).build())
//                            .toList();
//
//            // 모집 포지션 데이터
//            List<ProfileMetaDataDTO> positions =
//                    recruitingBand.getRecruitingPositions().stream().map(PositionMappingEntity::getPosition).toList()
//                            .stream().map(position -> ProfileMetaDataDTO.builder().key(position.getkey()).value(position.getValue()).build())
//                            .toList();
//
//            // 합주 지역 데이터
//            List<DistrictDataDTO> districts =
//                    recruitingBand.getDistricts().stream().map(districtMappingEntity ->
//                            DistrictDataDTO.builder()
//                                    .districtId(districtMappingEntity.getDistrict().getId())
//                                    .districtName(districtMappingEntity.getDistrict().getDistrictName())
//                                    .build()
//                    ).toList();
//
//            bandRecruitInfoList.add(
//                    BandRecruitInfoResponseDTO.builder()
//                            .bandId(recruitingBand.getId())
//                            .bandName(recruitingBand.getBandName())
//                            .recruitTitle(recruitingBand.getBandRecruitInfoEntity().getTitle())
//                            .genres(musicGenres)
//                            .positions(positions)
//                            .districts(districts)
//                            .build()
//            );
//        }
    }

    @Transactional // TODO 중간에 에러 났을때 id 값은 increment 돼있는거 왜 그런지 확인 필요
    public void registerBandProfile(UserEntity user, RegisterBandProfileRequestDTO profileParam) {
        UserEntity userEntity = userRepository.findById(user.getId()).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        MetadataEnumRepository.verifyMetadataKey(profileParam.genre(), MusicGenre.class);
        List<DistrictEntity> districts = verifyDistrict(profileParam.district());

        BandEntity bandEntity =
                BandEntity.builder()
                        .user(userEntity)
                        .bandName(profileParam.bandName())
                        .introduction(profileParam.introduction())
                        .exposeYn(profileParam.exposeYn())
                        .recruitYn(profileParam.recruitYn())
                        .build();

        List<MusicGenreMappingEntity> musicGenreMappingEntityList = profileParam.genre().stream().map(genre ->
                MusicGenreMappingEntity.builder()
                        .type(MappingType.BAND)
                        .band(bandEntity)
                        .genre(MusicGenre.valueOf(genre))
                        .build()).toList();

        List<DistrictMappingEntity> districtMappingEntityList = districts.stream().map(district ->
                DistrictMappingEntity.builder()
                        .type(MappingType.BAND)
                        .band(bandEntity)
                        .district(district)
                        .build()).toList();

        bandRepository.save(bandEntity);
        musicGenreMappingRepository.saveAll(musicGenreMappingEntityList);
        districtMappingRepository.saveAll(districtMappingEntityList);

        // 밴드 멤버 저장
        if (!profileParam.bandMember().isEmpty()) {
            List<BandMemberEntity> bandMemberEntityList = new ArrayList<>();
            for (BandMemberDTO bandMember : profileParam.bandMember()) {
                UserEntity member;
                if (bandMember.userId() == userEntity.getId()) {    // 나 자신 일때
                    member = userEntity;
                } else {
                    member = userRepository.findById(bandMember.userId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_USER));
                }
                bandMemberEntityList.add(BandMemberEntity.builder().band(bandEntity).user(member).position(Position.valueOf(bandMember.positionCode())).build());
            }
            bandMemberEntityRepository.saveAll(bandMemberEntityList);
        }

        // 모집 정보 저장
        if (profileParam.recruitYn()) {
            // 모집 포지션
            MetadataEnumRepository.verifyMetadataKey(profileParam.recruitPosition(), Position.class);
            List<PositionMappingEntity> positionMappingEntityList = profileParam.recruitPosition().stream().map(position ->
                    PositionMappingEntity.builder()
                            .type(MappingType.BAND)
                            .band(bandEntity)
                            .position(Position.valueOf(position))
                            .build()).toList();
            positionMappingRepository.saveAll(positionMappingEntityList);

            // 모집 내용
            BandRecruitInfoEntity recruitInfoEntity =
                    BandRecruitInfoEntity.builder()
                            .band(bandEntity)
                            .title(profileParam.recruitTitle())
                            .description(profileParam.recruitDescription())
                            .build();
            bandRecruitInfoRepository.save(recruitInfoEntity);
        }
    }

    private List<DistrictEntity> verifyDistrict(List<Long> districts) {
        List<DistrictEntity> districtEntityList = districtRepository.findByIdIn(districts);
        if (districts.size() != districtEntityList.size()) {
            throw new BusinessException(ErrorCode.NOT_EXIST_CODE);
        }
        return districtEntityList;
    }

}
