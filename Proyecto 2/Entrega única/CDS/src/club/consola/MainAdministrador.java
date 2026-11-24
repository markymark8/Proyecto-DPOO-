package club.consola;


public class MainAdministrador {

    public static void main(String[] args) {
        ArranqueClub.ejecutar("administradores", u -> u.esAdministrador());
    }
}
