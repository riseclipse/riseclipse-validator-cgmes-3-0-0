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
package fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.validator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.cim.CimPackage;
import fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.cim.provider.CimItemProviderAdapterFactory;
import fr.centralesupelec.edf.riseclipse.cim.cgmes_v3_0_0.cim.utilities.CimModelLoader;
import fr.centralesupelec.edf.riseclipse.cim.headerModel.ModelDescription.ModelDescriptionPackage;
import fr.centralesupelec.edf.riseclipse.util.AbstractRiseClipseConsole;
import fr.centralesupelec.edf.riseclipse.util.FileRiseClipseConsole;
import fr.centralesupelec.edf.riseclipse.util.IRiseClipseConsole;
import fr.centralesupelec.edf.riseclipse.util.RiseClipseMessage;
import fr.centralesupelec.edf.riseclipse.util.Severity;
import fr.centralesupelec.edf.riseclipse.util.TextRiseClipseConsole;
import fr.centralesupelec.edf.riseclipse.validation.ocl.OCLValidator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.EValidator.SubstitutionLabelProvider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.ocl.pivot.validation.ComposedEValidator;

public class RiseClipseValidatorCGMES {

    private static final String TOOL_VERSION = "1.0.1 (13 May 2022)";

    private static final String HELP_OPTION             = "help";
    private static final String HELP_ENVIRONMENT_OPTION = "help-environment";
    private static final String ERROR_OPTION            = "error";
    private static final String WARNING_OPTION          = "warning";
    private static final String NOTICE_OPTION           = "notice";
    private static final String INFO_OPTION             = "info";
    private static final String DEBUG_OPTION            = "debug";
    private static final String MERGE_OPTION            = "merge";
    private static final String OUTPUT_OPTION           = "output";
    private static final String FORMAT_OPTION           = "format-string";
    private static final String USE_COLOR_OPTION        = "use-color";
    private static final String USE_FILENAMES_STARTING_WITH_DOT_OPTION = "use-filenames-starting-with-dot";
    
    private static final String RISECLIPSE_VARIABLE_PREFIX                    = "RISECLIPSE_";
    private static final String CONSOLE_LEVEL_VARIABLE_NAME                   = RISECLIPSE_VARIABLE_PREFIX + "CONSOLE_LEVEL";
    private static final String MERGE_VARIABLE_NAME                           = RISECLIPSE_VARIABLE_PREFIX + "MERGE";
    private static final String OUTPUT_FILE_VARIABLE_NAME                     = RISECLIPSE_VARIABLE_PREFIX + "OUTPUT_FILE";
    private static final String USE_COLOR_VARIABLE_NAME                       = RISECLIPSE_VARIABLE_PREFIX + "USE_COLOR";
    private static final String USE_FILENAMES_STARTING_WITH_DOT_VARIABLE_NAME = RISECLIPSE_VARIABLE_PREFIX + "USE_FILENAMES_STARTING_WITH_DOT";
    private static final String FORMAT_STRING_VARIABLE_NAME                   = RISECLIPSE_VARIABLE_PREFIX + "FORMAT_STRING";

    private static final String FALSE_VARIABLE_VALUE = "FALSE";

//  private static final String EMERGENCY_KEYWORD = "EMERGENCY";
//  private static final String ALERT_KEYWORD     = "ALERT";
//  private static final String CRITICAL_KEYWORD  = "CRITICAL";
    private static final String ERROR_KEYWORD       = "ERROR";
    private static final String WARNING_KEYWORD     = "WARNING";
    private static final String NOTICE_KEYWORD      = "NOTICE";
    private static final String INFO_KEYWORD        = "INFO";
    private static final String DEBUG_KEYWORD       = "DEBUG";

    private static final String VALIDATOR_CIM_CATEGORY = "CIM/Validator";
    private static final String INFO_FORMAT_STRING = "%6$s%1$-4s%7$s: %4$s";
    
    private static final String OCL_FILE_EXTENSION = ".ocl";

    private static @NonNull Options options = new Options();

    private static List< @NonNull String> oclFiles = new ArrayList<>();
    private static List< @NonNull String > cimFiles = new ArrayList<>();

    private static Severity consoleLevel = Severity.WARNING;
    private static String formatString = "%6$s%1$-7s%7$s: [%2$s] %4$s (%5$s:%3$d)";
    private static boolean useColor = false;
    private static boolean keepDotFiles = false;
    private static boolean merge = false;
    private static String outputFile;

    private static OCLValidator oclValidator;
    private static CimItemProviderAdapterFactory adapter;
    private static CimModelLoader loader;

    private static void usage() {
        new HelpFormatter().printUsage( new PrintWriter( System.out, true ), 120, "RiseClipseValidatorCGMES", options );
        System.exit( -1 );
    }

    private static void help() {
        displayLegal();
        
        IRiseClipseConsole console = AbstractRiseClipseConsole.getConsole();
        console.setLevel( Severity.INFO );
        console.setFormatString( INFO_FORMAT_STRING );

        StringWriter buffer = new StringWriter();
        new HelpFormatter().printHelp( new PrintWriter( buffer ), 120, "RiseClipseValidatorCGMES", "", options, 0, 0, "" );
        for( String s : buffer.toString().split( "\n" )) {
            console.info( VALIDATOR_CIM_CATEGORY, 0, s );
        }
        System.exit( -1 );
    }

    private static void helpEnvironment() {
        IRiseClipseConsole console = AbstractRiseClipseConsole.getConsole();
        console.setLevel( Severity.INFO );
        console.setFormatString( INFO_FORMAT_STRING );
        
        displayLegal();
        
        console.info( VALIDATOR_CIM_CATEGORY, 0,
                      "The folowing environment variables may be used in addition to command line options, "
                    + "however, the latter have precedence." );
        console.info( VALIDATOR_CIM_CATEGORY, 0,
                      "\t" + CONSOLE_LEVEL_VARIABLE_NAME + ": if its value is one of (ignoring case) "
                    + ERROR_KEYWORD + ", " + WARNING_KEYWORD + ", " + NOTICE_KEYWORD + ", " + INFO_KEYWORD + " or " + DEBUG_KEYWORD
                    + ", then the corresponding level is set, otherwise the variable is ignored." );
        console.info( VALIDATOR_CIM_CATEGORY, 0,
                      "\t" + OUTPUT_FILE_VARIABLE_NAME + ": name of the output file for messages." );
        console.info( VALIDATOR_CIM_CATEGORY, 0,
                      "\t" + MERGE_VARIABLE_NAME + ": if its value is not equal to FALSE "
                    + "(ignoring case), it is equivalent to the use of " + MERGE_OPTION + " option." );
        console.info( VALIDATOR_CIM_CATEGORY, 0,
                      "\t" + FORMAT_STRING_VARIABLE_NAME + ": string used to format messages (see description of " + FORMAT_OPTION + " option)." );
        console.info( VALIDATOR_CIM_CATEGORY, 0,
                      "\t" + USE_COLOR_VARIABLE_NAME + ": if its value is not equal to FALSE "
                    + "(ignoring case), it is equivalent to the use of " + USE_COLOR_OPTION + " option." );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "\t" + USE_FILENAMES_STARTING_WITH_DOT_VARIABLE_NAME + ": if its value is not equal to FALSE "
                    + "(ignoring case), it is equivalent to the use of " + USE_FILENAMES_STARTING_WITH_DOT_OPTION + " option." );
        System.exit( 0 );
    }
    
    private static void setOptionsFromEnvironmentVariables() {
        String s = System.getenv( CONSOLE_LEVEL_VARIABLE_NAME );
        if( s != null ) {
            if( s.equalsIgnoreCase( ERROR_KEYWORD )) {
                consoleLevel  = Severity.ERROR;
            }
            else if( s.equalsIgnoreCase( WARNING_KEYWORD )) {
                consoleLevel  = Severity.WARNING;
            }
            else if( s.equalsIgnoreCase( NOTICE_KEYWORD )) {
                consoleLevel  = Severity.NOTICE;
            }
            else if( s.equalsIgnoreCase( INFO_KEYWORD )) {
                consoleLevel  = Severity.INFO;
            }
            else if( s.equalsIgnoreCase( DEBUG_KEYWORD )) {
                consoleLevel  = Severity.DEBUG;
            }
            else {
                AbstractRiseClipseConsole.getConsole().warning(
                    VALIDATOR_CIM_CATEGORY, 0,
                    "Value of environment variable " + CONSOLE_LEVEL_VARIABLE_NAME + " is not recognized and ignored" );
            }
        }
        
        outputFile = System.getenv( OUTPUT_FILE_VARIABLE_NAME );
        
        s = System.getenv( FORMAT_STRING_VARIABLE_NAME );
        if( s != null ) {
            formatString = s;
        }
        
        s = System.getenv( USE_COLOR_VARIABLE_NAME );
        if( s != null ) {
            if( ! s.equalsIgnoreCase( FALSE_VARIABLE_VALUE )) {
                useColor = true;
            }
        }
        
        s = System.getenv( MERGE_VARIABLE_NAME );
        if( s != null ) {
            if( ! s.equalsIgnoreCase( FALSE_VARIABLE_VALUE )) {
                merge  = true;
            }
        }
        
        s = System.getenv( USE_FILENAMES_STARTING_WITH_DOT_VARIABLE_NAME );
        if( s != null ) {
            if( ! s.equalsIgnoreCase( FALSE_VARIABLE_VALUE )) {
                keepDotFiles = true;
            }
        }
    }
    
    public static void displayLegal() {
        IRiseClipseConsole console = AbstractRiseClipseConsole.getConsole();
        Severity oldLevel = console.setLevel( Severity.INFO );
        String oldFormat = console.setFormatString( INFO_FORMAT_STRING );

        console.info( VALIDATOR_CIM_CATEGORY, 0, "Copyright (c) 2022 CentraleSupélec & EDF." );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "All rights reserved. This program and the accompanying materials" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "are made available under the terms of the Eclipse Public License v2.0" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "which accompanies this distribution, and is available at" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "https://www.eclipse.org/legal/epl-v20.html" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "This tool is part of RiseClipse." );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "Contributors:" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "    Computer Science Department, CentraleSupélec" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "    EDF R&D" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "Contacts:" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "    dominique.marcadet@centralesupelec.fr" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "    aurelie.dehouck-neveu@edf.fr" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "Web site:" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "    https://riseclipse.github.io/" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "" );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "RiseClipseValidatorCGMES3 version: " + TOOL_VERSION );
        console.info( VALIDATOR_CIM_CATEGORY, 0, "" );

        console.setFormatString( oldFormat );
        console.setLevel( oldLevel );
    }

    public static void main( String[] args ) {
        setOptionsFromEnvironmentVariables();
        
        options.addOption( Option.builder( "h" )
                                 .longOpt( HELP_OPTION )
                                 .desc( "display help message" )
                                 .build() );
        options.addOption( Option.builder( "h" )
                                 .longOpt( HELP_ENVIRONMENT_OPTION )
                                 .desc( "display environment variables used" )
                                 .build() );
        options.addOption( Option.builder()
                                 .longOpt( ERROR_OPTION )
                                 .desc( "display only error messages" )
                                 .build() );
        options.addOption( Option.builder()
                                  .longOpt( WARNING_OPTION )
                                  .desc( "display warning and error messages" )
                                  .build() );
        options.addOption( Option.builder()
                                 .longOpt( NOTICE_OPTION )
                                 .desc( "display notice, warning and error messages" )
                                 .build() );
        options.addOption( Option.builder()
                                 .longOpt( INFO_OPTION )
                                 .desc( "display info, notice, warning and error messages" )
                                 .build() );
        options.addOption( Option.builder()
                                 .longOpt( DEBUG_OPTION )
                                 .desc( "display all messages" )
                                 .build() );
        options.addOption( Option.builder()
                                 .longOpt( MERGE_OPTION )
                                 .desc( "all ENTSO-E CGMES v3.0.0 files are merged before OCL validation" )
                                 .build() );
        options.addOption( Option.builder( "o" )
                                 .longOpt( OUTPUT_OPTION )
                                 .hasArg()
                                 .argName( "file" )
                                 .desc( "all messages are written to the given file" )
                                 .build() );
        options.addOption( Option.builder()
                                 .longOpt( FORMAT_OPTION )
                                 .hasArg()
                                 .argName( "format" )
                                 .desc( "messages are outputed with a java.util.Formatter using the given format string,"
                                      + "1$ is severity, 2$ is category, 3$ is line number, 4$ is message, 5$ is filename,"
                                      + "6$ is color start, 7$ is color end (these last two are only used if the --" + USE_COLOR_OPTION + " option is active),"
                                      + "default is '%6$s%1$-7s%7$s: [%2$s] %4$s (%5$s:%3$d)'.")
                                 .build() );
        options.addOption( Option.builder()
                                 .longOpt( USE_COLOR_OPTION )
                                 .desc( "colors (using ANSI escape sequences) are used when displaying messages" )
                                 .build() );
        options.addOption( Option.builder()
                                 .longOpt( USE_FILENAMES_STARTING_WITH_DOT_OPTION )
                                 .desc( "files whose name begins with a dot are not ignored" )
                                 .build() );

        if( args.length == 0 ) usage();
        
        CommandLine cmd = null;
        try {
            cmd = new DefaultParser().parse( options, args );
        }
        catch( ParseException e ) {
            usage();
            return;
        }

        IRiseClipseConsole console = AbstractRiseClipseConsole.getConsole();
        if( cmd.hasOption(             USE_COLOR_OPTION )) useColor = true;
        if( useColor ) {
            console = new TextRiseClipseConsole( true );
        }
        if( cmd.hasOption(                  HELP_OPTION )) help();
        if( cmd.hasOption(      HELP_ENVIRONMENT_OPTION )) helpEnvironment();

        if( cmd.hasOption(   ERROR_OPTION )) consoleLevel = Severity.ERROR;
        if( cmd.hasOption( WARNING_OPTION )) consoleLevel = Severity.WARNING;
        if( cmd.hasOption(  NOTICE_OPTION )) consoleLevel = Severity.NOTICE;
        if( cmd.hasOption(    INFO_OPTION )) consoleLevel = Severity.INFO;
        if( cmd.hasOption(   DEBUG_OPTION )) consoleLevel = Severity.DEBUG;
        console.setLevel( consoleLevel );

        if( cmd.hasOption( MERGE_OPTION )) merge = true;
        if( cmd.hasOption( USE_FILENAMES_STARTING_WITH_DOT_OPTION )) keepDotFiles = true;
        
        if( cmd.hasOption( OUTPUT_OPTION )) {
            outputFile = cmd.getOptionValue( OUTPUT_OPTION );
        }
        if( outputFile != null ) {
            @NonNull
            Severity level = console.getLevel();
            console = new FileRiseClipseConsole( outputFile );
            console.setLevel( level );
        }
        if( cmd.hasOption( FORMAT_OPTION )) {
            formatString = cmd.getOptionValue( FORMAT_OPTION );
        }
        console.setFormatString( formatString );
        
        displayLegal();

        for( String f : cmd.getArgs() ) {
            getFiles( Paths.get( f ));
        }
        prepare( oclFiles );
        if( merge ) {
            for( int i = 0; i < cimFiles.size(); ++i ) {
                load( cimFiles.get( i ));
            }
            loader.finalizeLoad( console );
            run();
        }
        else {
            for( int i = 0; i < cimFiles.size(); ++i ) {
                loader.reset();
                load( cimFiles.get( i ));
                loader.finalizeLoad( console );
                run();
            }
        }
    }
    
    private static void getFiles( Path path ) {
        IRiseClipseConsole console = AbstractRiseClipseConsole.getConsole();
        if( path.getName( path.getNameCount() - 1 ).toString().startsWith( "." )) {
            if( ! keepDotFiles ) {
                console.info( VALIDATOR_CIM_CATEGORY, 0, path, " is ignored because it starts with a dot" );
                return;
            }
        }
        if( Files.isDirectory( path )) {
            try {
                Files.list( path )
                    .forEach( f -> getFiles( f.normalize() ));
            }
            catch( IOException e ) {
                console.error( VALIDATOR_CIM_CATEGORY, 0, "got IOException while listing content of directory ", path );
            }
        }
        else if( Files.isReadable( path )) {
            String name = path.toString();
            int dotPos = name.lastIndexOf( "." );
            if( dotPos != -1 ) {
                if( name.substring( dotPos ).equalsIgnoreCase( OCL_FILE_EXTENSION )) {
                    oclFiles.add( name );
                }
                else {
                    cimFiles.add( name );
                }
            }
            else {
                cimFiles.add( name );
            }
        }
        else {
            console.error( VALIDATOR_CIM_CATEGORY, 0, "Cannot read file ", path );
        }
        
    }

    public static void prepare( List< String > oclFiles ) {
        @NonNull IRiseClipseConsole console = AbstractRiseClipseConsole.getConsole();
        
        CimPackage cimPkg = CimPackage.eINSTANCE;
        if( cimPkg == null ) {
            console.emergency( VALIDATOR_CIM_CATEGORY, 0, "CIM package not found" );
            return;
        }
        ModelDescriptionPackage modelPkg = ModelDescriptionPackage.eINSTANCE;
        if( modelPkg == null ) {
            console.emergency( VALIDATOR_CIM_CATEGORY, 0, "ModelDescription package not found" );
        }

        ComposedEValidator validator = ComposedEValidator.install( cimPkg );

        if( oclFiles != null ) {
            oclValidator = new OCLValidator( cimPkg, console );

            for( int i = 0; i < oclFiles.size(); ++i ) {
                oclValidator.addOCLDocument( oclFiles.get( i ), console );
            }
            oclValidator.prepare( validator, console );
        }

        loader = new CimModelLoader( );
        adapter = new CimItemProviderAdapterFactory();
    }

    public static void run() {
        IRiseClipseConsole console = AbstractRiseClipseConsole.getConsole();
        for( Resource resource : loader.getResourceSet().getResources() ) {
            // Some empty resources may be created when other URI are present
            if( ! resource.getContents().isEmpty() ) {
                console.info( VALIDATOR_CIM_CATEGORY, 0, "Validating file: ", resource.getURI().lastSegment() );
                validate( resource, adapter );
            }
        }
    }

    private static void validate( @NonNull Resource resource, final AdapterFactory adapter ) {
        IRiseClipseConsole console = AbstractRiseClipseConsole.getConsole();
        
        Map< Object, Object > context = new HashMap<>();
        SubstitutionLabelProvider substitutionLabelProvider = new EValidator.SubstitutionLabelProvider() {

            @Override
            public String getValueLabel( EDataType eDataType, Object value ) {
                return Diagnostician.INSTANCE.getValueLabel( eDataType, value );
            }

            @Override
            public String getObjectLabel( EObject eObject ) {
                IItemLabelProvider labelProvider = ( IItemLabelProvider ) adapter.adapt( eObject,
                        IItemLabelProvider.class );
                return labelProvider.getText( eObject );
            }

            @Override
            public String getFeatureLabel( EStructuralFeature eStructuralFeature ) {
                return Diagnostician.INSTANCE.getFeatureLabel( eStructuralFeature );
            }
        };
        context.put( EValidator.SubstitutionLabelProvider.class, substitutionLabelProvider );

        for( int n = 0; n < resource.getContents().size(); ++n ) {
            Diagnostic diagnostic = Diagnostician.INSTANCE.validate( resource.getContents().get( n ), context );

            for( Iterator< Diagnostic > i = diagnostic.getChildren().iterator(); i.hasNext(); ) {
                Diagnostic childDiagnostic = i.next();
                
                List< ? > data = childDiagnostic.getData();
                EObject object = ( EObject ) data.get( 0 );
                String message = childDiagnostic.getMessage();
                String[] parts = message.split( ";" );
                if(( parts.length == 4 ) && ( parts[1].startsWith( "OCL" ))) {
                    // This should be an OCL message with the new format
                    Severity severity = Severity.ERROR;
                    try {
                        severity = Severity.valueOf( parts[0] );
                    }
                    catch( IllegalArgumentException ex ) {}
                    int line = 0;
                    try {
                        line = Integer.valueOf( parts[2] );
                    }
                    catch( NumberFormatException ex ) {}
                    console.output( new RiseClipseMessage( severity, parts[1], line, parts[3] ));
                    continue;
                }

                if(( data.size() > 1 ) && ( data.get( 1 ) instanceof EAttribute )) {
                    EAttribute attribute = ( EAttribute ) data.get( 1 );
                    if( attribute == null ) continue;
                    message = "\tAttribute " + attribute.getName() + " of "
                                + substitutionLabelProvider.getObjectLabel( object ) + " : "
                                + childDiagnostic.getChildren().get( 0 ).getMessage();
                }

                switch( childDiagnostic.getSeverity() ) {
                case Diagnostic.INFO:
                    console.info( VALIDATOR_CIM_CATEGORY, 0, message );
                    break;
                case Diagnostic.WARNING:
                    console.warning( VALIDATOR_CIM_CATEGORY, 0, message );
                    break;
                case Diagnostic.ERROR:
                    console.error( VALIDATOR_CIM_CATEGORY, 0, message );
                    break;
                default:
                    break;
                }
            }
        }
    }

    private static void load( String cimFile ) {
        loader.loadWithoutValidation( cimFile );
    }

    public static void resetLoadFinalize( String cimFile ) {
        loader.reset();
        load( cimFile );
        IRiseClipseConsole console = AbstractRiseClipseConsole.getConsole();
        loader.finalizeLoad( console );
    }

}

