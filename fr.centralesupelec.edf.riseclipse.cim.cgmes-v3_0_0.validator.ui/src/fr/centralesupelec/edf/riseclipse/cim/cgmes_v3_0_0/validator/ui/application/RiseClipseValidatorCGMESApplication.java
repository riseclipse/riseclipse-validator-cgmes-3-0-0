/*
*************************************************************************
**  Copyright (c) 2022 CentraleSupélec & EDF.
**  All rights reserved. This program and the accompanying materials
**  are made available under the terms of the Eclipse Public License v2.0
**  which accompanies this distribution, and is available at
**  https://www.eclipse.org/legal/epl-v20.html
** 
**  This file is part of the RiseClipse tool
**  
**  Contributors:
**      Computer Science Department, CentraleSupélec
**      EDF R&D
**  Contacts:
**      dominique.marcadet@centralesupelec.fr
**      aurelie.dehouck-neveu@edf.fr
**  Web site:
**      https://riseclipse.github.io
*************************************************************************
*/
package fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.validator.ui.application;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.validator.ui.component.CgmesFilePane;
import fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.validator.ui.component.TreeFilePane;

import javax.swing.JScrollPane;
import javax.swing.JPanel;

public class RiseClipseValidatorCGMESApplication {

    private JFrame frame;
    private TreeFilePane oclTree;

    /**
     * Launch the application.
     */
    public static void main( String[] args ) {
        EventQueue.invokeLater( new Runnable() {
            public void run() {
                try {
                    RiseClipseValidatorCGMESApplication window = new RiseClipseValidatorCGMESApplication();
                    window.frame.setVisible( true );
                }
                catch( Exception e ) {
                    e.printStackTrace();
                }
            }
        } );
    }

    /**
     * Create the application.
     */
    public RiseClipseValidatorCGMESApplication() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle( "RiseClipseValidatorCGMESApplication" );
        frame.setBounds( 100, 100, 800, 600 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        JTabbedPane tabbedPane = new JTabbedPane( JTabbedPane.TOP );
        frame.getContentPane().add( tabbedPane );

        JPanel cgmesPanel = new CgmesFilePane( this );
        tabbedPane.addTab( "CGMES Files", null, cgmesPanel, null );

        JScrollPane oclPane = new JScrollPane();
        tabbedPane.addTab( "OCL Files", null, oclPane, null );

        File fileRoot = new File( System.getProperty( "user.dir" ) + "/OCL" );
        oclTree = new TreeFilePane( fileRoot );
        oclPane.setViewportView( oclTree );

    }

    public ArrayList< File > getOclFiles() {
        ArrayList< File > oclFiles = new ArrayList<>();
        oclTree.getSelectedFiles( oclFiles );
        return oclFiles;
    }

}
