//ALİ BAŞKÖYLÜ
//Grafik ve grafik elemanları için swing kütüphanesi kullanacağız
import javax.swing.*;
//Tablo verimizi kolayca siniflar arası aktarmak için
import javax.swing.table.DefaultTableModel;
//Arayüzler vs. için awt kütüphanesine ihtiyacımız var
import java.awt.*;
//Eventler(tıklama gibi) için awt.evente ihtiyacımız var
import java.awt.event.*;
//Veritabanı SQL bağlantısı için
import java.sql.*;
//Rastgele sebep göstermek için
import java.util.Random;
//Süre işlemleri ve kaydı için
import java.time.*;
//Anlık değişen süre ekranı için
import java.util.Timer;
import java.util.TimerTask;
//Resim göstermek için
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
//hata
import java.io.IOException;

class Veritabani{
    //Veritabani konumu
    String konum = "jdbc:sqlite:veritabani.db";
    //Veritabanina su an bagli miyiz
    Boolean bagli = false;

    Connection baglanti;

    DefaultTableModel sebepler;

    int sonBirakma;

    Veritabani(){
        try {
            //SQLite Sürücümüzü tanıtalım
            DriverManager.registerDriver(new org.sqlite.JDBC());

            //Veritabanı bağlantımızı kurmayı deneyelim
            baglanti = DriverManager.getConnection(konum);

            if (baglanti != null) {
                //Başarıyla bağlanırsak konsoldan geri bildirim alalım
                System.out.println("Veritabanına başarıyla bağlanıldı.");
                this.bagli = true;
            }
        } catch (SQLException hata) {
            //Bağlanamazsak sebebini konsola yazdıralım
            System.out.println(hata.getMessage());
            this.bagli = false;
        }
    }
    //ÖNEMLİ: TABLODAKİ HÜCREDE DEĞİŞİKLİKLER ENTER TUŞUNA BASMADAN GEÇERLİ OLMAZ. AYRICA BOŞ HÜCRE SİLME İŞLEMİ ANLAMINA GELİR.
    DefaultTableModel SebepleriCek(){
        if(!this.bagli){
            System.out.println("Önce veritabanına bağlı olduğunuzdan emin olun.");
            return null;
        }
        //Tablomuzda mevcut verileri gösterecek modelimizi tanımlamakla başlayalım
        sebepler = new DefaultTableModel(new String[]{"Sebep"}, 0);

        String sql="SELECT * FROM sebepler";

        try {
            //SQL sorgumuzu calistirip verileri modele yazalim
            Statement ifade = baglanti.createStatement();

            ResultSet sonuclar = ifade.executeQuery(sql);

            while(sonuclar.next())
            {
                String sebep = sonuclar.getString("metin");
                sebepler.addRow(new Object[]{sebep});
            }
        } catch (SQLException hata) {
            //SQL sorgumuz calismazsa sebebini konsola yazdıralım
            System.out.println(hata.getMessage());
            this.bagli = false;
        }

        return sebepler;
    }

    Boolean SebepEkle(String sebep){
        if(!this.bagli){
            System.out.println("Önce veritabanına bağlı olduğunuzdan emin olun.");
            return null;
        }
        try {
            String sql = "INSERT INTO sebepler(metin) VALUES(?)";
            //Hazir ifademizi tanimlayalim
            PreparedStatement hazirIfade = baglanti.prepareStatement(sql);
            //? işaretini stringimizle değiştirdik
            hazirIfade.setString(1, sebep);
            //Calistiralim
            hazirIfade.executeUpdate();
            sebepler.addRow(new Object[]{sebep});
            return true;
        } catch (SQLException hata) {
            //SQL sorgumuz calismazsa sebebini konsola yazdıralım
            System.out.println(hata.getMessage());
            this.bagli = false;
            return false;
        }
    }
    //ÖNEMLİ: TABLODAKİ HÜCREDE DEĞİŞİKLİKLER ENTER TUŞUNA BASMADAN GEÇERLİ OLMAZ. AYRICA BOŞ HÜCRE SİLME İŞLEMİ ANLAMINA GELİR.
    Boolean SebepleriGuncelle(){
        if(!this.bagli){
            System.out.println("Önce veritabanına bağlı olduğunuzdan emin olun.");
            return null;
        }
        try {
            //Önce her şeyi silelim, sonra tablodaki verileri veritabanına geçirelim
            String sql="DELETE FROM sebepler";
            Statement ifade = baglanti.createStatement();
            ifade.execute(sql);
            
            sql = "INSERT INTO sebepler(metin) VALUES(?)";
            //Hazir ifademizi tanimlayalim
            PreparedStatement hazirIfade = baglanti.prepareStatement(sql);

            //Hızlı bir sorgu paketi için otomatik commiti kapatalım.
            baglanti.setAutoCommit(false);

            //Tablodaki her sebep için sorgu paketine ekleme yapalım
            for (int i = 0; i < sebepler.getRowCount(); i++) {
                String sebep = sebepler.getValueAt(i, 0).toString();
                //Eğer tablodaki satırdaki yazı silinmişse biz de kökten silelim kaydı
                if(sebep != null && sebep.length() > 0){
                    hazirIfade.setString(1, sebep);

                    hazirIfade.addBatch();
                }
            }
            //sorgu paketini bitirelim
            hazirIfade.executeBatch();

            //Elle güncellemeleri commit edip otomatik commiti tekrar açalım
            baglanti.commit();
            baglanti.setAutoCommit(true);

            return true;
        } catch (SQLException hata) {
            //SQL sorgumuz calismazsa sebebini konsola yazdıralım
            System.out.println(hata.getMessage());
            this.bagli = false;
            return false;
        }
    }
    //SIFIRLA
    Boolean sayacSifirla(){
        if(!this.bagli){
            System.out.println("Önce veritabanına bağlı olduğunuzdan emin olun.");
            return null;
        }
        int simdi = (int)Instant.now().getEpochSecond();
        try {
            String sql = "INSERT INTO birakma_kaydi(timestamp) VALUES(?)";
            //Hazir ifademizi tanimlayalim
            PreparedStatement hazirIfade = baglanti.prepareStatement(sql);
            //? işaretini stringimizle değiştirdik
            hazirIfade.setInt(1, simdi);
            //Calistiralim
            hazirIfade.executeUpdate();

            //Local veriyi de duzenleyelim
            this.sonBirakma = simdi;

            return true;
        } catch (SQLException hata) {
            //SQL sorgumuz calismazsa sebebini konsola yazdıralım
            System.out.println(hata.getMessage());
            this.bagli = false;
            return false;
        }
    }

    //Sayaç değerini veritabanından çek
    int sayacCek(){
        if(!this.bagli){
            System.out.println("Önce veritabanına bağlı olduğunuzdan emin olun.");
            return 0;
        }

        String sql="SELECT * FROM birakma_kaydi";

        try {
            //SQL sorgumuzu calistirip son birakma kaydini dondurelim
            Statement ifade = baglanti.createStatement();

            ResultSet sonuclar = ifade.executeQuery(sql);

            int kayit = 0;

            while(sonuclar.next())
            {
                kayit = sonuclar.getInt("timestamp");
            }

            this.sonBirakma = kayit;//SON KAYIT

        } catch (SQLException hata) {
            //SQL sorgumuz calismazsa sebebini konsola yazdıralım
            System.out.println(hata.getMessage());
            this.bagli = false;
            this.sonBirakma = (int)Instant.now().getEpochSecond();
        }

        return this.sonBirakma;
    }

}

class Motivasyon{
    Motivasyon(Veritabani veritabani){
        //Rastgele bir sebep seçip göstereceğiz
        //0 dan satır sayısına kadar rastgele bir rakamı indis olarak ele alalım
        int idx = new Random().nextInt(veritabani.sebepler.getRowCount());

        //o indisteki sebebi motivasyon mesajı olarak gösterelim
        JOptionPane.showMessageDialog(new JFrame(), veritabani.sebepler.getValueAt(idx, 0).toString(), "Motivasyon Mesajı - Pes Etme - İşte Sebebin!",
        JOptionPane.INFORMATION_MESSAGE);
    }
}

class Sebepler{
    JFrame pencere;
    Sebepler(Veritabani veritabani){
        //Penceremizi oluşturalım
        pencere = new JFrame("Sebepleri Düzenle | Sigarasız Kod V1.0 - Ali Başköylü");

        //Kapatma ile beraber veriyi güncelleyelim. Boş satırları da silelim.
        pencere.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("Sebep düzenle ekranı kapandı. Veritabanı güncellenecek.");
                veritabani.SebepleriGuncelle();
                e.getWindow().dispose();
            }
        });

        //Tablomuzu tablo verisinden oluşturalım
        JTable tablo = new JTable(veritabani.SebepleriCek());

        //Tablomuzdaki veriler ekrana sığmazsa yan çubuktan aşağı çekilebilmesi için tablomuzu önce bir kayan panele(ScrollPane)'e ekliyoruz. 
        JScrollPane kayanPanel = new JScrollPane(tablo);

        //Tablomuzu içeren kayan paneli pencereye yukarda kalacak sekilde ekleyelim
        pencere.add(kayanPanel,BorderLayout.NORTH);  

        //Tablomuzu içeren pencerenin boyutunu ayarlayalım
        pencere.setSize(800,400);  

        //Penceremizi ortalayalım
        pencere.setLocationRelativeTo(null);

        //Pencereyi görünür hale getirelim
        pencere.setVisible(true);
    }
}

class Anasayfa{
    JFrame pencere; 
    JButton sayac;

    Anasayfa(Veritabani veritabani){
        //Penceremizi oluşturalım
        pencere = new JFrame("Sigarasız Kod V1.0 - Ali Başköylü");
        //Anasayfa penceresinden kapatma çarpısına tıklanırsa yazılımı bu sefer TAMAMEN kapatalım
        pencere.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel ustPanel = new JPanel();

        //Butonları tanımlayalım
        JButton sebepDuzenle=new JButton("Sebepleri Düzenle");
        JButton sebepEkle=new JButton("Sebep Ekle");

        ustPanel.add(sebepDuzenle);
        ustPanel.add(sebepEkle);

        JPanel altPanel = new JPanel();

        JButton motiveEt = new JButton("Motive Et");

        altPanel.add(motiveEt);


        JButton sifirla;

        sifirla=new JButton("SAYACI SIFIRLA");

        sayac=new JButton("YÜKLENİYOR LÜTFEN BEKLEYİN...");
        //font
        sayac.setFont(new Font("Arial", Font.BOLD, 20));

        //Butonlara tıklanınca ne olacağını tanımlayalım
        //Sebep ekle/düzenle butonları
        sebepDuzenle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //Tıklandığında sebepler nesnesini yani sebepler penceresini açacak
                new Sebepler(veritabani);
            }
        });

        sebepEkle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //Tıklandığında bir giriş kutusu ile sebep eklenebilecek
                String sebepGirdisi = JOptionPane.showInputDialog("Lütfen sigarayı neden bıraktığınıza dair bir sebep girin: ","Çocuklarım beni daha rahat ve daha iyi örnek alabilsin istedim.");
                if(sebepGirdisi != null && (sebepGirdisi.length() > 0)){
                    veritabani.SebepEkle(sebepGirdisi);
                }else{
                    System.out.println("Bir sebep girilmedi.");
                }
                
            }
        });

        //Motive et butonu
        motiveEt.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //Tıklandığında sebeplerimizden rastgele bir tane seçip bizi motive edecek
                new Motivasyon(veritabani);
            }
        });

        //Sayaç sıfırlama butonu
        sifirla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //Tıklandığında şu anın timestamp değeri veritabaninda guncellenecek
                veritabani.sayacSifirla();
            }
        });       

        //Elemanlarımızı BorderLayout ile pencerede kuzey, güney, doğu, batı ve merkez konumlarına koyalım.
        pencere.add(ustPanel,BorderLayout.NORTH);  
        pencere.add(altPanel,BorderLayout.SOUTH);  
        
        pencere.add(sifirla,BorderLayout.WEST);  
        pencere.add(sayac,BorderLayout.CENTER);  
        
        //Pencere boyutumuzu ayarlayalım
        pencere.setSize(1150,550);  

        //Penceremizi ortalayalım
        pencere.setLocationRelativeTo(null);

        //Penceremizi görünür hale getirelim
        pencere.setVisible(true);
    }
}

//SigarasizKod sinifimiz arayuzu ve arayuz elementlerini tutuyor
class SigarasizKod{
    public static void main(String args[]){
       //Veritabani sınıfını çağırarak veritabanı bağlantısını kuralım
       Veritabani veritabani = new Veritabani();

       //İlk veri senkronizasyonunu yapalım
       veritabani.SebepleriCek();
       veritabani.sayacCek();

       //Anasayfa pencerisinin sınıfını çağırarak yazılım arayüzünü başlatalım
       Anasayfa anasayfa = new Anasayfa(veritabani);

       //Geçen süre sayacı
       Timer sayac = new Timer();

       //Anlik guncellenen gecen sure ekrani
       sayac.schedule(new TimerTask() {
            @Override
            public void run() {
                int simdi =  (int)Instant.now().getEpochSecond();

                int temelFark = simdi - veritabani.sonBirakma;

                int dakikaFark = temelFark/60;

                int saatFark = dakikaFark/60;

                int gunFark = saatFark/24;

                int ayFark = gunFark/30;

                int yilFark = ayFark/12;

                anasayfa.sayac.setText("Sigarayı bırakalı " + String.valueOf(temelFark%60) +
                                                " saniye, " + String.valueOf(dakikaFark%60) +
                                                " dakika, " + String.valueOf(saatFark%24) +
                                                " saat, " + String.valueOf(gunFark%30) +
                                                " gün, " + String.valueOf(ayFark%12) +
                                                " ay, " + String.valueOf(yilFark) +
                                                " yıl oldu! TEBRİKLER!");
            }
       }, 2500, 1000);       
    }
}