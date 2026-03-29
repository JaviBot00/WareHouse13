package com.hotguy.warehouse13.controller;

import com.hotguy.warehouse13.model.Producto;

import java.util.ArrayList;
import java.util.List;

public class DataAccess {
    private static final String data = """
        Clase=Producto;codigoProducto=TECL5678X;descripcion=Teclado mecánico RGB con switches rojos;precio=89.99;stock=45
        Clase=Producto;codigoProducto=RATN9012K;descripcion=Ratón inalámbrico ergonómico con 5 botones;precio=34.50;stock=67
        Clase=Producto;codigoProducto=AURC3456L;descripcion=Auriculaes inalámbricos con cancelación de ruido;precio=129.99;stock=23
        Clase=Producto;codigoProducto=WEBC7890P;descripcion=Webcam Full HD 1080p con micrófono integrado;precio=59.90;stock=32
        Clase=Producto;codigoProducto=HUBB2345M;descripcion=Hub USB 3.0 de 4 puertos con alimentación;precio=24.75;stock=56
        Clase=Producto;codigoProducto=DISK1234R;descripcion=Disco duro externo 1TB USB-C resistente al agua;precio=79.99;stock=18
        Clase=Producto;codigoProducto=MONS4567T;descripcion=Monitor portátil 15.6 pulgadas Full HD;precio=189.50;stock=12
        Clase=Producto;codigoProducto=PADT8901Y;descripcion=Alfombrilla de ratón XXL con base de goma;precio=19.99;stock=89
        Clase=Producto;codigoProducto=MICR2345U;descripcion=Micrófono USB de condensador para streaming;precio=65.30;stock=27
        Clase=Producto;codigoProducto=COOL6789I;descripcion=Base refrigeradora para portátil con 3 ventiladores;precio=29.95;stock=41
        Clase=Producto;codigoProducto=CARG5678O;descripcion=Cargador rápido USB-C 65W con 2 puertos;precio=45.80;stock=34
        Clase=Producto;codigoProducto=LAPD9012P;descripcion=Soporte ajustable para portátil de aluminio;precio=39.99;stock=50""";

    public static List<Producto> loadData() {
        List<Producto> listaProductosBase = new ArrayList<>();

        String[] lineas = data.split("\n");
        for (String ln : lineas) {
            listaProductosBase.add(Producto.cargarDesdeCSVPlus(ln));
        }


        return listaProductosBase;
    }
}
