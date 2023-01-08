/*
 * Licensed under the EUPL-1.2-or-later.
 * Copyright (c) 2023, gridDigIt Kft. All rights reserved.
 * @author Chavdar Ivanov
 */
package core;

import org.apache.jena.riot.adapters.RDFWriterRIOT;

// Model.write adapter - must be public.
public class RDFWriterCIM extends RDFWriterRIOT { public RDFWriterCIM() { super("RDFXMLCIM") ; } }