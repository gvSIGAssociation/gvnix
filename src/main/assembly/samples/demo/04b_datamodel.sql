--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: cliente; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cliente (
    id bigint NOT NULL,
    activo boolean NOT NULL,
    direccion_postal character varying(255),
    nombre_completo character varying(255),
    password character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    version integer
);


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('hibernate_sequence', 1, false);


--
-- Name: linea_pedido; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE linea_pedido (
    id bigint NOT NULL,
    cantidad integer NOT NULL,
    precio real,
    version integer,
    pedido bigint,
    producto bigint,
    CONSTRAINT linea_pedido_cantidad_check CHECK (((cantidad >= 1) AND (cantidad <= 99)))
);


--
-- Name: pedido; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE pedido (
    id bigint NOT NULL,
    identificador_pedido character varying(255) NOT NULL,
    total real,
    version integer,
    cliente bigint NOT NULL
);


--
-- Name: producto; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE producto (
    id bigint NOT NULL,
    descripcion character varying(255),
    detalles character varying(255),
    fin timestamp without time zone,
    identificador character varying(255) NOT NULL,
    inicio timestamp without time zone,
    nombre character varying(255) NOT NULL,
    precio real NOT NULL,
    version integer
);


--
-- Data for Name: cliente; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cliente (id, activo, direccion_postal, nombre_completo, password, username, version) FROM stdin;
\.


--
-- Data for Name: linea_pedido; Type: TABLE DATA; Schema: public; Owner: -
--

COPY linea_pedido (id, cantidad, precio, version, pedido, producto) FROM stdin;
\.


--
-- Data for Name: pedido; Type: TABLE DATA; Schema: public; Owner: -
--

COPY pedido (id, identificador_pedido, total, version, cliente) FROM stdin;
\.


--
-- Data for Name: producto; Type: TABLE DATA; Schema: public; Owner: -
--

COPY producto (id, descripcion, detalles, fin, identificador, inicio, nombre, precio, version) FROM stdin;
\.


--
-- Name: cliente_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cliente
    ADD CONSTRAINT cliente_pkey PRIMARY KEY (id);


--
-- Name: linea_pedido_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY linea_pedido
    ADD CONSTRAINT linea_pedido_pkey PRIMARY KEY (id);


--
-- Name: pedido_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY pedido
    ADD CONSTRAINT pedido_pkey PRIMARY KEY (id);


--
-- Name: producto_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY producto
    ADD CONSTRAINT producto_pkey PRIMARY KEY (id);


--
-- Name: fk89ec90d71d47a1cc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY linea_pedido
    ADD CONSTRAINT fk89ec90d71d47a1cc FOREIGN KEY (pedido) REFERENCES pedido(id);


--
-- Name: fk89ec90d76e554d82; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY linea_pedido
    ADD CONSTRAINT fk89ec90d76e554d82 FOREIGN KEY (producto) REFERENCES producto(id);


--
-- Name: fkc4dd174544b800f2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pedido
    ADD CONSTRAINT fkc4dd174544b800f2 FOREIGN KEY (cliente) REFERENCES cliente(id);


--
-- PostgreSQL database dump complete
--

