/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.constant;

/**
 *
 * @author Vi$ky
 */
public class VoiceDestination {
     public static String getVoicePath(int val,String fileName) {
            String path = "";
            String filepath = "C:\\ipssi\\LocTracker\\waveform\\";
            
//            waveMap.put(, "0101001");	
//waveMap.put(, "0100002");
//waveMap.put( , "0100003");
//waveMap.put( , "0100004");
//waveMap.put( , "0100005");
//waveMap.put( , "0100006");
//waveMap.put(Status.TPRQuestions.minesAndTransporterFromChallan, "0101007");
//waveMap.put(Status.TPRQuestions.tarpaulinOk , "0101008");
//waveMap.put(Status.TPRQuestions.sealOk , "0101009");
//waveMap.put(Status.TPRQuestions.numberVisible , "0101010");
//waveMap.put(Status.TPRQuestions.sideMirror , "0101011");
//waveMap.put(Status.TPRQuestions.reverseHornOk , "0101021");
//waveMap.put(Status.TPRQuestions.hornPlay, "0101022");
//waveMap.put(Status.TPRQuestions.pushBrake, "0101023");
//waveMap.put(Status.TPRQuestions.brakeLightOn, "0101024");
//waveMap.put(Status.TPRQuestions.headLightOk, "0101025");
//waveMap.put(Status.TPRQuestions.headLightOn, "0101026");
//waveMap.put(Status.TPRQuestions.leftSideIndicator, "0101027");
//waveMap.put(Status.TPRQuestions.leftSideIndicatorOn, "0101028");
//waveMap.put(Status.TPRQuestions.rightSideIndicator, "0101029");
//waveMap.put(Status.TPRQuestions.rightSideIndicatorOn, "0101030");
//waveMap.put(Status.TPRQuestions.enterDriverIdAndDriverName, "0101041");
//waveMap.put(Status.TPRQuestions.driverAppearsDrunk, "0101051");
//waveMap.put(Status.TPRQuestions.goToWB, "0101052");
//waveMap.put(, "0101053");
//waveMap.put(, "0101054");
//waveMap.put(, "0101055");
//waveMap.put(, "0101056");
//waveMap.put(, "0101057");

                        switch (val) {
//                case Status.TPRQuestion.seatBeltWorm:
//                    path = filepath + fileName;
//                    break;
//                case Status.TPRQuestions.cleanFinger:
//                    path = filepath + fileName;
//                    break;
//                case Status.TPRQuestions.thankYou:
//                    path = filepath + fileName;
//                    break;
//                case Status.TPRQuestions.tryAgainFinger:
//                    path = filepath + fileName;
//                    break;
//                case Status.TPRQuestions.getGpsRepairedForGateOut:
//                    path = filepath + fileName;
//                    break;
//                case Status.TPRQuestions.leftSideIndicator:
//                    path = filepath + fileName;
//                    break;
//                case Status.TPRQuestions.seatBeltWorm:
//                    path = filepath + fileName;
//                    break;
//                case Status.TPRQuestions.tryOnceAgainFinger:
//                    path = filepath + fileName;
//                    break;
//                case Status.TPRQuestions.qcStampDonegoParking:
//                    path = filepath + fileName;
//                    break;
//                case Status.TPRQuestions.getGpsRepaired:
//                    path = filepath + fileName;
//                    break;
//                 case Status.TPRQuestions.getQcStamp:
//                    path = filepath + fileName;
//                    break;   
//                   case Status.TPRQuestions.dumpCoal:
//                    path = filepath + fileName;
//                    break;  
//                   case Status.TPRQuestions.InformControlRoom:
//                    path = filepath + fileName;
//                    break;  
//                   case Status.TPRQuestions.goToRestrationCenter:
//                    path = filepath + fileName;
//                    break;  
//                         case Status.TPRQuestions.fingerNotMatch:
//                    path = filepath + fileName;
//                    break;  
//                              case Status.TPRQuestions.paperNotValid:
//                    path = filepath + fileName;
//                    break;  
//                case Status.TPRQuestions.getDriverRegistration:
//                    path = filepath + fileName;
//                    break;  
//                    case Status.TPRQuestions.updateFingerPrint:
//                    path = filepath + fileName;
//                    break;  
//                case Status.TPRQuestions.challanEntry:
//                    path = filepath + fileName;
//                    break;  
//                    case Status.TPRQuestions.issueRfidTag:
//                    path = filepath + fileName;
//                    break;   
//                    case Status.TPRQuestions.registrationNewVehicle:
//                    path = filepath + fileName;
//                    break;  
//                    case Status.TPRQuestions.fixedLr:
//                    path = filepath + fileName;
//                    break;  
//                    case Status.TPRQuestion.fixedLr:
//                    path = filepath + fileName;
//                    break;  
//                        case Status.TPRQuestion.fixedLr:
//                    path = filepath + fileName;
//                    break;  
//                            case Status.TPRQuestion.fixedLr:
//                    path = filepath + fileName;
//                    break;  
//                                case Status.TPRQuestion.fixedLr:
//                    path = filepath + fileName;
//                    break;  
//                                    case Status.TPRQuestion.fixedLr:
//                    path = filepath + fileName;
//                    break;  
//                                        case Status.TPRQuestion.fixedLr:
//                    path = filepath + fileName;
//                    break;  
//                                            case Status.TPRQuestion.fixedLr:
//                    path = filepath + fileName;
//                    break;  
//                                                case Status.TPRQuestion.fixedLr:
//                    path = filepath + fileName;
//                    break;  
//                        
//                        
                                                      
                default:
                    path = filepath + fileName;
                    break;
            }
            return path;
            
            
            
//            waveMap.put(Status.TPRQuestion.seatBeltWorm, "0101001");	
//waveMap.put(Status.TPRQuestion.cleanFinger, "0100002");
//waveMap.put(Status.TPRQuestion.thankYou , "0100003");
//waveMap.put(Status.TPRQuestion.tryAgainFinger , "0100004");
//waveMap.put(Status.TPRQuestion.tryOnceAgainFinger , "0100005");
//waveMap.put(Status.TPRQuestion.fingerNotMatch , "0100006");
//waveMap.put(Status.TPRQuestion.minesAndTransporterFromChallan, "0101007");
//waveMap.put(Status.TPRQuestion.tarpaulinOk , "0101008");
//waveMap.put(Status.TPRQuestion.sealOk , "0101009");
//waveMap.put(Status.TPRQuestion.numberVisible , "0101010");
//waveMap.put(Status.TPRQuestion.sideMirror , "0101011");
//waveMap.put(Status.TPRQuestion.reverseHornOk , "0101021");
//waveMap.put(Status.TPRQuestion.hornPlay, "0101022");
//waveMap.put(Status.TPRQuestion.pushBrake, "0101023");
//waveMap.put(Status.TPRQuestion.brakeLightOn, "0101024");
//waveMap.put(Status.TPRQuestion.headLightOk, "0101025");
//waveMap.put(Status.TPRQuestion.headLightOn, "0101026");
//waveMap.put(Status.TPRQuestion.leftSideIndicator, "0101027");
//waveMap.put(Status.TPRQuestion.leftSideIndicatorOn, "0101028");
//waveMap.put(Status.TPRQuestion.rightSideIndicator, "0101029");
//waveMap.put(Status.TPRQuestion.rightSideIndicatorOn, "0101030");
//waveMap.put(Status.TPRQuestion.enterDriverIdAndDriverName, "0101041");
//waveMap.put(Status.TPRQuestion.driverAppearsDrunk, "0101051");
//waveMap.put(Status.TPRQuestion.goToWB, "0101052");
//waveMap.put(Status.TPRQuestion.vehicleBlackListed, "0101053");
//waveMap.put(Status.TPRQuestion.fixedLr, "0101054");
//waveMap.put(Status.TPRQuestion.registrationNewVehicle, "0101055");
//waveMap.put(Status.TPRQuestion.issueRfidTag, "0101056");
//waveMap.put(Status.TPRQuestion.challanEntry, "0101057");
//waveMap.put(Status.TPRQuestion.updateFingerPrint, "0101058");
//waveMap.put(Status.TPRQuestion.getDriverRegistration, "0101059");
//waveMap.put(Status.TPRQuestion.paperNotValid, "0101060");
//waveMap.put(Status.TPRQuestion.goToRestrationCenter, "0101061");
//waveMap.put(Status.TPRQuestion.InformControlRoom, "0101062");
//waveMap.put(Status.TPRQuestion.dumpCoal, "0300001");
//waveMap.put(Status.TPRQuestion.getQcStamp, "0300031");
//waveMap.put(Status.TPRQuestion.getGpsRepairedForGateOut, "0300032");
//waveMap.put(Status.TPRQuestion.getGpsRepaired, "0400001");
//waveMap.put(Status.TPRQuestion.qcStampDonegoParking, "0400002");
            
            
            
           
        }


    
}
