//package serialization.protobuf;
//
//import java.io.*;
//
//public class ProtobufTest {
//    public static void main(String[] args) throws IOException {
//        PersonOuterClass.Person person = PersonOuterClass.Person.newBuilder()
//                .setAge(18).setUsername("Shawyer").setSex("male").build();
//
//        OutputStream os = new FileOutputStream(new File("C:\\Users\\ShawyerPeng\\Desktop\\test.txt"));
//        person.writeTo(os);
//        FileInputStream fis = new FileInputStream(new File("C:\\Users\\ShawyerPeng\\Desktop\\test.txt"));
//        PersonOuterClass.Person person1 = PersonOuterClass.Person.parseFrom(fis);
//        System.out.println(person1);
//    }
//}
