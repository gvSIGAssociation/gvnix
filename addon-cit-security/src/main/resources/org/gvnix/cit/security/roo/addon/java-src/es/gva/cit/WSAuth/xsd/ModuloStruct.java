/**
 *  GV-CIT terceros: Gestión de datos personales, domicilios, cuentas, versiones y relaciones.
 *
 *  Copyright (C) 2009 - CONSELLERIA D'INFRAESTRUCTURES I TRANSPORT
 *                       GENERALITAT VALENCIANA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 *  You may obtain a copy of the License at http://www.gnu.org/licenses/gpl-2.0.html
 */

package es.gva.cit.WSAuth.xsd;

/**
 *
 * @author rgarcia <a href="mailto:rgarcia@disid.com">Ricardo Garcia
 *         Fernandez</a>
 *
 */
public class ModuloStruct implements java.io.Serializable {
    private java.lang.String modulo;

    private java.lang.String valor;

    private java.lang.String descrip;

    private java.lang.String tipo;

    public ModuloStruct() {
    }

    public ModuloStruct(
           java.lang.String modulo,
           java.lang.String valor,
           java.lang.String descrip,
           java.lang.String tipo) {
           this.modulo = modulo;
           this.valor = valor;
           this.descrip = descrip;
           this.tipo = tipo;
    }


    /**
     * Gets the modulo value for this ModuloStruct.
     *
     * @return modulo
     */
    public java.lang.String getModulo() {
        return modulo;
    }


    /**
     * Sets the modulo value for this ModuloStruct.
     *
     * @param modulo
     */
    public void setModulo(java.lang.String modulo) {
        this.modulo = modulo;
    }


    /**
     * Gets the valor value for this ModuloStruct.
     *
     * @return valor
     */
    public java.lang.String getValor() {
        return valor;
    }


    /**
     * Sets the valor value for this ModuloStruct.
     *
     * @param valor
     */
    public void setValor(java.lang.String valor) {
        this.valor = valor;
    }


    /**
     * Gets the descrip value for this ModuloStruct.
     *
     * @return descrip
     */
    public java.lang.String getDescrip() {
        return descrip;
    }


    /**
     * Sets the descrip value for this ModuloStruct.
     *
     * @param descrip
     */
    public void setDescrip(java.lang.String descrip) {
        this.descrip = descrip;
    }


    /**
     * Gets the tipo value for this ModuloStruct.
     *
     * @return tipo
     */
    public java.lang.String getTipo() {
        return tipo;
    }


    /**
     * Sets the tipo value for this ModuloStruct.
     *
     * @param tipo
     */
    public void setTipo(java.lang.String tipo) {
        this.tipo = tipo;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ModuloStruct)) return false;
        ModuloStruct other = (ModuloStruct) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.modulo==null && other.getModulo()==null) ||
             (this.modulo!=null &&
              this.modulo.equals(other.getModulo()))) &&
            ((this.valor==null && other.getValor()==null) ||
             (this.valor!=null &&
              this.valor.equals(other.getValor()))) &&
            ((this.descrip==null && other.getDescrip()==null) ||
             (this.descrip!=null &&
              this.descrip.equals(other.getDescrip()))) &&
            ((this.tipo==null && other.getTipo()==null) ||
             (this.tipo!=null &&
              this.tipo.equals(other.getTipo())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getModulo() != null) {
            _hashCode += getModulo().hashCode();
        }
        if (getValor() != null) {
            _hashCode += getValor().hashCode();
        }
        if (getDescrip() != null) {
            _hashCode += getDescrip().hashCode();
        }
        if (getTipo() != null) {
            _hashCode += getTipo().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ModuloStruct.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://WSAuth.cit.gva.es/xsd", "ModuloStruct"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modulo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "modulo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valor");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valor"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("descrip");
        elemField.setXmlName(new javax.xml.namespace.QName("", "descrip"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tipo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "tipo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType,
           java.lang.Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType,
           java.lang.Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
