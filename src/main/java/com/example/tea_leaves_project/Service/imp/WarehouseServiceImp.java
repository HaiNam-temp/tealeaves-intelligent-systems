package com.example.tea_leaves_project.Service.imp;

import com.example.tea_leaves_project.DTO.PackageDto;
import com.example.tea_leaves_project.DTO.WarehouseDto;
import com.example.tea_leaves_project.DTO.WarehousePackageDto;
import com.example.tea_leaves_project.Exception.ApiException;
import com.example.tea_leaves_project.Model.entity.Package;
import com.example.tea_leaves_project.Model.entity.Warehouse;
import com.example.tea_leaves_project.Payload.Request.WeighRequest;
import com.example.tea_leaves_project.Payload.Response.QrResponse;
import com.example.tea_leaves_project.Payload.ResponseData;
import com.example.tea_leaves_project.Responsitory.PackageRepository;
import com.example.tea_leaves_project.Responsitory.WarehouseRepository;
import com.example.tea_leaves_project.Service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class WarehouseServiceImp implements WarehouseService {
    @Autowired
    WarehouseRepository warehouseRepository;
    @Autowired
    PackageRepository packageRepository;
    public QrResponse extractDataFromQR(String qrCode) {
        String lengpackid=qrCode.substring(0,2);
        int leng=Integer.parseInt(lengpackid);
        String packageid=qrCode.substring(2,leng+2);
        long packageId=Long.parseLong(packageid);

        qrCode=qrCode.substring(leng+2);
        String lenguserid=qrCode.substring(0,2);
        int leng2=Integer.parseInt(lenguserid);
        String userid=qrCode.substring(2,leng2+2);
        long userId=Long.parseLong(userid);

        qrCode=qrCode.substring(leng2+2);
        String lengwarehouseid=qrCode.substring(0,2);
        int leng3=Integer.parseInt(lengwarehouseid);
        String warehouseid=qrCode.substring(2,leng3+2);
        long warehouseId=Long.parseLong(warehouseid);

        qrCode=qrCode.substring(leng3+2);
        String lengcreatedate=qrCode.substring(0,2);
        int leng4=Integer.parseInt(lengcreatedate);
        String createdate=qrCode.substring(2,leng4+2);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date createDate = new Date();
        try{
            createDate=formatter.parse(createdate);
        }catch (ParseException e){
            log.warn("Error String to Date "+ e.getMessage());
        }
        qrCode=qrCode.substring(leng4+2);
        String lengtypecode=qrCode.substring(0,2);
        int leng5=Integer.parseInt(lengtypecode);
        String teacode=qrCode.substring(2,leng5+2);

        return QrResponse.builder().packageid(packageId)
                .userid(userId)
                .warehouseid(warehouseId)
                .createtime(createDate)
                .teacode(teacode)
                .build();
    }

    // tính số bao chè sẵn sàng vận chuyển
    public long calculateTotalPackage(Warehouse warehouse) {
        long sum=0;
        for(Package p : warehouse.getPackages()){
            if(p.getStatus().equals("Wait delivery")) sum++;
        }
        return sum;
    }

    @Override
    public List<WarehouseDto> getAllWarehouse() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<WarehouseDto> warehouseDtos = new ArrayList<>();
        for (Warehouse warehouse : warehouses) {
            WarehouseDto warehouseDto=WarehouseDto.builder()
                    .warehouseid(warehouse.getWarehouseid())
                    .name(warehouse.getName())
                    .address(warehouse.getAddress())
                    .lat(warehouse.getLat())
                    .lon(warehouse.getLon())
                    .totalpackage(calculateTotalPackage(warehouse))
                    .currentcapacity(warehouse.getCurrent_capacity())
                    .totalcapacity(warehouse.getTotal_capacity())
                    .build();
            warehouseDtos.add(warehouseDto);
        }
        return warehouseDtos;
    }

    @Override
    public WarehousePackageDto getPackageByWarehouse(long warehouseid) {

        Warehouse warehouse=warehouseRepository.findByWarehouseid(warehouseid);

        if(warehouse==null){
            throw ApiException.ErrDataLoss().build();
        }

        List<Package> plist=packageRepository.findByWarehouse(warehouse);
        List<PackageDto> packageDtoList= new ArrayList<>();

        for(Package p : plist){

            PackageDto packageDto=PackageDto.builder()
                    .packageId(p.getPackageid())
                    .fullname(p.getUser().getFullname())
                    .warehouse(p.getWarehouse().getName())
                    .createdtime(p.getCreatedtime())
                    .weightime(p.getWeightime())
                    .capacity(p.getCapacity())
                    .unit(p.getUtil())
                    .status(p.getStatus())
                    .teacode(p.getTypetea().getTeacode())
                    .build();
            packageDtoList.add(packageDto);
        }
         WarehousePackageDto warehousePackageDto=WarehousePackageDto.builder()
               .warehouseid(warehouseid)
               .name(warehouse.getName())
               .packages(packageDtoList).build();
        return warehousePackageDto;
    }
    @Override
    public QrResponse scanQrCode (String qrcode){
            QrResponse qrResponse=extractDataFromQR(qrcode);
            long packageid=qrResponse.getPackageid();
            Package p=packageRepository.findByPackageid(packageid);
            if(p==null){
                throw ApiException.ErrDataLoss().build();
            }
            if(p.getStatus().equals("Weighn't yet")){
                p.setStatus("Scanning");
                packageRepository.save(p);
                qrResponse.setMessage("Scan thành công");
            }
            if(p.getStatus().equals("Scanning") || p.getStatus().equals("Wait delivery")){
                qrResponse.setMessage("Sản phẩm đã được quét");
            }
            return qrResponse;
    }
    @Override
    public ResponseData Weigh(WeighRequest weighRequest) {
        System.out.println(weighRequest.getWeight()+" "+weighRequest.getBin_code());
        String bin_code=weighRequest.getBin_code();
        ResponseData responseData = new ResponseData();
        responseData.resp();
        Warehouse warehouse=warehouseRepository.findByBincode(bin_code);
        if(warehouse==null){
            responseData.setMessage("Khong ton tai can");
            return responseData;
        }
        if(warehouse==null){
            throw ApiException.ErrDataLoss().build();
        }
        List<Package> p=packageRepository.findByStatusAndWarehouse("Scanning",warehouse);
            if(p.size()==0){
                responseData.setMessage("Không tìm thấy bao scan gần nhất");
                System.out.println("Không tìm thấy bao scan gần nhất");
                return responseData;
            }
            if(p.size()>1){
                for(Package ps:p){
                    ps.setStatus("Weighn't yet");
                    packageRepository.save(ps);
                }
                Package plast=p.getLast();
                plast.setStatus("Scanning");
                packageRepository.save(plast);
            }
                Package plast = p.getLast();
                plast.setStatus("Wait delivery");
                plast.setCapacity(weighRequest.getWeight());
                packageRepository.save(plast);
                warehouse.setCurrent_capacity(warehouse.getCurrent_capacity() + weighRequest.getWeight());
                warehouseRepository.save(warehouse);
                responseData.setMessage("Cân thành công");
            return responseData;
    }
}
