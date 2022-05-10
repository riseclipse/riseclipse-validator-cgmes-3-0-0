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
package fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.validator.ui.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.validator.RiseClipseValidatorCGMES;
import fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.validator.ui.application.RiseClipseValidatorCGMESApplication;
import fr.centralesupelec.edf.riseclipse.util.AbstractRiseClipseConsole;
import fr.centralesupelec.edf.riseclipse.util.IRiseClipseConsole;

@SuppressWarnings( "serial" )
public class CgmesFilePane extends JPanel implements ActionListener {

    private JButton btnAddCgmesFile;
    private JButton btnValidate;
    private CgmesFileList cgmesFilesList;
    private RiseClipseValidatorCGMESApplication application;  // NOSONAR (application is not serializable)

    public CgmesFilePane( RiseClipseValidatorCGMESApplication application ) {
        this.application = application;
        
        setLayout( new BorderLayout( 0, 0 ));

        JPanel btnPanel = new JPanel();
        add( btnPanel, BorderLayout.SOUTH );

        btnAddCgmesFile = new JButton( "Add CGMES file" );
        btnAddCgmesFile.addActionListener( this );
        btnPanel.add( btnAddCgmesFile );

        btnValidate = new JButton( "Validate" );
        btnValidate.addActionListener( this );
        btnPanel.add( btnValidate );

        JScrollPane cgmesFilesPane = new JScrollPane();
        add( cgmesFilesPane, BorderLayout.CENTER );
        
        cgmesFilesList = new CgmesFileList();
        cgmesFilesPane.setViewportView( cgmesFilesList );
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Object source = e.getSource();

        if( source == btnAddCgmesFile ) {
            JFrame frame = (JFrame) SwingUtilities.getRoot(( Component ) source );
            FileDialog fileDialog = new FileDialog( frame, "SCL - Choose a file" );
            fileDialog.setMode( FileDialog.LOAD );
            fileDialog.setVisible( true );
            if( fileDialog.getFiles().length != 0 ) {
                cgmesFilesList.add( fileDialog.getFiles()[0] );
            }
            return;
        }

        if( source == btnValidate ) {
            ArrayList< File > oclFiles = application.getOclFiles();
            List< String > oclFileNames =
                    oclFiles
                    .stream()
                    .map( File::getAbsolutePath )
                    .collect( Collectors.toList() );
            List< String > cgmesFiles = cgmesFilesList.getCgmesFiles();
            
            ResultFrame result = new ResultFrame();
            
            IRiseClipseConsole console = result.getMainConsole();
            AbstractRiseClipseConsole.changeConsole( console );
            RiseClipseValidatorCGMES.displayLegal( );
            RiseClipseValidatorCGMES.prepare( oclFileNames );
            result.repaint();
            for( String file : cgmesFiles ) {
                console = result.getConsoleFor( file );
                AbstractRiseClipseConsole.changeConsole( console );
                RiseClipseValidatorCGMES.resetLoadFinalize( file );
                RiseClipseValidatorCGMES.run();
                result.repaint();
            }
        }
    }
}
