#!/usr/bin/env python3
"""
Generador de Reportes HTML para Análisis de Calidad de Datos
Sistema POS & Gestión Integral - Motor de Predicciones ML

Autor: Ingeniero de Datos Senior
Fecha: 14 de octubre de 2025
Versión: 1.0.0

Descripción:
    Módulo complementario para generar reportes HTML interactivos con visualizaciones
    del análisis de calidad de datos.

Uso:
    from data_quality_html_report import HTMLReportGenerator
    generator = HTMLReportGenerator(quality_report_dict)
    generator.generate_html_report('reporte_calidad.html')
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import plotly.express as px
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import plotly.offline as pyo
from datetime import datetime
from typing import Dict, List, Any
import json
import base64
from io import BytesIO

class HTMLReportGenerator:
    """
    Generador de reportes HTML con visualizaciones interactivas.
    """
    
    def __init__(self, quality_report: Dict[str, Any]):
        """
        Inicializa el generador con el reporte de calidad.
        
        Args:
            quality_report (Dict): Diccionario con análisis de calidad completo
        """
        self.report = quality_report
        self.html_content = ""
        
    def generate_html_report(self, output_file: str = 'data_quality_report.html') -> str:
        """
        Genera reporte HTML completo con visualizaciones.
        
        Args:
            output_file (str): Archivo de salida HTML
            
        Returns:
            str: Contenido HTML generado
        """
        print(f"🎨 Generando reporte HTML: {output_file}")
        
        # Construir HTML
        html_parts = [
            self._generate_html_header(),
            self._generate_executive_summary_html(),
            self._generate_completeness_section(),
            self._generate_outliers_section(),
            self._generate_correlations_section(),
            self._generate_distributions_section(),
            self._generate_drift_section(),
            self._generate_recommendations_section(),
            self._generate_html_footer()
        ]
        
        self.html_content = ''.join(html_parts)
        
        # Guardar archivo
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(self.html_content)
        
        print(f"✅ Reporte HTML generado: {output_file}")
        return self.html_content
    
    def _generate_html_header(self) -> str:
        """Genera header HTML con estilos CSS."""
        return f"""
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reporte de Calidad de Datos - Sistema POS</title>
    <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
    <style>
        body {{
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
            color: #333;
        }}
        
        .container {{
            max-width: 1200px;
            margin: 0 auto;
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }}
        
        .header {{
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }}
        
        .header h1 {{
            margin: 0;
            font-size: 2.5em;
            font-weight: 300;
        }}
        
        .header .subtitle {{
            margin: 10px 0 0 0;
            font-size: 1.2em;
            opacity: 0.9;
        }}
        
        .content {{
            padding: 30px;
        }}
        
        .section {{
            margin-bottom: 40px;
            border-bottom: 1px solid #eee;
            padding-bottom: 30px;
        }}
        
        .section:last-child {{
            border-bottom: none;
        }}
        
        .section-title {{
            font-size: 1.8em;
            color: #667eea;
            margin-bottom: 20px;
            border-left: 4px solid #667eea;
            padding-left: 15px;
        }}
        
        .metric-card {{
            display: inline-block;
            background: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            margin: 10px;
            text-align: center;
            min-width: 150px;
            border-left: 4px solid #667eea;
        }}
        
        .metric-value {{
            font-size: 2em;
            font-weight: bold;
            color: #667eea;
        }}
        
        .metric-label {{
            color: #666;
            font-size: 0.9em;
            margin-top: 5px;
        }}
        
        .alert {{
            padding: 15px;
            border-radius: 5px;
            margin: 10px 0;
        }}
        
        .alert-success {{
            background-color: #d4edda;
            border-color: #c3e6cb;
            color: #155724;
        }}
        
        .alert-warning {{
            background-color: #fff3cd;
            border-color: #ffeaa7;
            color: #856404;
        }}
        
        .alert-danger {{
            background-color: #f8d7da;
            border-color: #f5c6cb;
            color: #721c24;
        }}
        
        .table {{
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }}
        
        .table th, .table td {{
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }}
        
        .table th {{
            background-color: #f8f9fa;
            font-weight: 600;
            color: #667eea;
        }}
        
        .table tr:hover {{
            background-color: #f5f5f5;
        }}
        
        .chart-container {{
            margin: 20px 0;
            padding: 20px;
            background-color: #fafafa;
            border-radius: 8px;
        }}
        
        .recommendations {{
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            margin: 20px 0;
        }}
        
        .recommendation {{
            margin: 10px 0;
            padding: 10px;
            border-left: 3px solid #667eea;
            background-color: white;
            border-radius: 0 5px 5px 0;
        }}
        
        .timestamp {{
            color: #999;
            font-size: 0.9em;
            text-align: center;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #eee;
        }}
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📊 Reporte de Calidad de Datos</h1>
            <div class="subtitle">Sistema POS & Gestión Integral - Motor de Predicciones ML</div>
            <div class="subtitle">Generado el {datetime.now().strftime('%d de %B de %Y a las %H:%M')}</div>
        </div>
        <div class="content">
"""

    def _generate_executive_summary_html(self) -> str:
        """Genera sección de resumen ejecutivo."""
        summary = self.report.get('executive_summary', {})
        metadata = self.report.get('metadata', {})
        
        quality_score = summary.get('data_quality_score', 0)
        
        # Determinar color basado en puntuación
        if quality_score >= 80:
            score_color = '#28a745'  # Verde
        elif quality_score >= 60:
            score_color = '#ffc107'  # Amarillo
        else:
            score_color = '#dc3545'  # Rojo
        
        html = f"""
            <div class="section">
                <h2 class="section-title">📋 Resumen Ejecutivo</h2>
                
                <div class="metric-card">
                    <div class="metric-value" style="color: {score_color};">{quality_score}</div>
                    <div class="metric-label">Puntuación de Calidad</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{metadata.get('dataframe_shape', [0, 0])[0]:,}</div>
                    <div class="metric-label">Registros</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{metadata.get('total_features', 0)}</div>
                    <div class="metric-label">Características</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{metadata.get('numeric_features', 0)}</div>
                    <div class="metric-label">Numéricas</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{metadata.get('categorical_features', 0)}</div>
                    <div class="metric-label">Categóricas</div>
                </div>
        """
        
        # Alertas basadas en flags de calidad
        flags = summary.get('quality_flags', {})
        
        if flags.get('high_missing_data'):
            html += """
                <div class="alert alert-warning">
                    ⚠️ <strong>Datos Faltantes:</strong> Se detectaron niveles altos de datos faltantes que podrían afectar la calidad del modelo.
                </div>
            """
        
        if flags.get('excessive_outliers'):
            html += """
                <div class="alert alert-warning">
                    🎯 <strong>Outliers Excesivos:</strong> Se encontraron outliers en cantidades que podrían indicar problemas de calidad de datos.
                </div>
            """
        
        if flags.get('multicollinearity_issues'):
            html += """
                <div class="alert alert-danger">
                    🔗 <strong>Multicolinealidad:</strong> Se detectaron variables altamente correlacionadas que podrían afectar el rendimiento del modelo.
                </div>
            """
        
        if flags.get('data_drift_detected'):
            html += """
                <div class="alert alert-warning">
                    ⏰ <strong>Data Drift:</strong> Se detectó deriva temporal en los datos que podría requerir reentrenamiento del modelo.
                </div>
            """
        
        if not any(flags.values()):
            html += """
                <div class="alert alert-success">
                    ✅ <strong>Calidad Excelente:</strong> Los datos presentan buena calidad general sin problemas críticos detectados.
                </div>
            """
        
        html += "</div>"
        return html

    def _generate_completeness_section(self) -> str:
        """Genera sección de análisis de completitud."""
        completeness = self.report.get('detailed_analysis', {}).get('completeness', {})
        
        if not completeness:
            return ""
        
        overall_completeness = completeness.get('overall_completeness_pct', 0)
        summary_df = completeness.get('completeness_summary')
        
        html = f"""
            <div class="section">
                <h2 class="section-title">📊 Análisis de Completitud</h2>
                
                <p><strong>Completitud General:</strong> {overall_completeness}%</p>
        """
        
        if summary_df is not None and not summary_df.empty:
            # Tabla de completitud por columna
            html += """
                <h3>Completitud por Característica</h3>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Característica</th>
                            <th>Valores Completos</th>
                            <th>Valores Faltantes</th>
                            <th>% Completitud</th>
                        </tr>
                    </thead>
                    <tbody>
            """
            
            # Mostrar solo las primeras 15 filas o todas si son menos
            display_df = summary_df.head(15)
            
            for _, row in display_df.iterrows():
                completeness_pct = row['porcentaje_completo']
                row_class = ""
                if completeness_pct < 90:
                    row_class = "style='background-color: #fff3cd;'"
                elif completeness_pct < 95:
                    row_class = "style='background-color: #f8f9fa;'"
                
                html += f"""
                    <tr {row_class}>
                        <td>{row['columna']}</td>
                        <td>{row['valores_completos']:,}</td>
                        <td>{row['valores_faltantes']:,}</td>
                        <td>{completeness_pct:.2f}%</td>
                    </tr>
                """
            
            html += """
                    </tbody>
                </table>
            """
            
            if len(summary_df) > 15:
                html += f"<p><em>Mostrando las primeras 15 características de {len(summary_df)} totales.</em></p>"
        
        # Columnas problemáticas
        problematic = completeness.get('problematic_columns', [])
        if problematic:
            html += f"""
                <h3>Características con Problemas de Completitud (&lt; 95%)</h3>
                <ul>
            """
            for col in problematic[:10]:  # Mostrar máximo 10
                html += f"<li>{col}</li>"
            html += "</ul>"
            
            if len(problematic) > 10:
                html += f"<p><em>... y {len(problematic) - 10} características más.</em></p>"
        
        html += "</div>"
        return html

    def _generate_outliers_section(self) -> str:
        """Genera sección de análisis de outliers."""
        outliers = self.report.get('detailed_analysis', {}).get('outliers', {})
        
        if not outliers:
            return ""
        
        summary = outliers.get('summary', {})
        total_outliers = summary.get('total_outliers_detected', 0)
        columns_with_outliers = summary.get('columns_with_outliers', [])
        
        html = f"""
            <div class="section">
                <h2 class="section-title">🎯 Análisis de Outliers</h2>
                
                <div class="metric-card">
                    <div class="metric-value">{total_outliers:,}</div>
                    <div class="metric-label">Total Outliers</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{len(columns_with_outliers)}</div>
                    <div class="metric-label">Columnas Afectadas</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('percentage_columns_with_outliers', 0)}%</div>
                    <div class="metric-label">% Columnas con Outliers</div>
                </div>
        """
        
        if columns_with_outliers:
            html += """
                <h3>Características con Outliers Detectados</h3>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Característica</th>
                            <th>Outliers Detectados</th>
                            <th>% Outliers</th>
                            <th>Método</th>
                        </tr>
                    </thead>
                    <tbody>
            """
            
            outliers_by_column = outliers.get('outliers_by_column', {})
            method = outliers.get('method_used', 'IQR')
            
            for col in columns_with_outliers[:15]:  # Mostrar máximo 15
                col_data = outliers_by_column.get(col, {})
                outliers_count = col_data.get('outliers_detectados', 0)
                outliers_pct = col_data.get('porcentaje_outliers', 0)
                
                row_class = ""
                if outliers_pct > 5:
                    row_class = "style='background-color: #f8d7da;'"
                elif outliers_pct > 2:
                    row_class = "style='background-color: #fff3cd;'"
                
                html += f"""
                    <tr {row_class}>
                        <td>{col}</td>
                        <td>{outliers_count:,}</td>
                        <td>{outliers_pct:.2f}%</td>
                        <td>{method}</td>
                    </tr>
                """
            
            html += """
                    </tbody>
                </table>
            """
            
            if len(columns_with_outliers) > 15:
                html += f"<p><em>Mostrando las primeras 15 características de {len(columns_with_outliers)} con outliers.</em></p>"
        
        html += "</div>"
        return html

    def _generate_correlations_section(self) -> str:
        """Genera sección de análisis de correlaciones."""
        correlations = self.report.get('detailed_analysis', {}).get('correlations', {})
        
        if not correlations:
            return ""
        
        summary = correlations.get('summary', {})
        high_corr = correlations.get('high_correlations', [])
        multicollinearity = correlations.get('multicollinearity_issues', [])
        
        html = f"""
            <div class="section">
                <h2 class="section-title">🔗 Análisis de Correlaciones</h2>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('total_pairs', 0):,}</div>
                    <div class="metric-label">Pares Evaluados</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('high_correlation_pairs', 0)}</div>
                    <div class="metric-label">Correlaciones Altas</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('multicollinearity_pairs', 0)}</div>
                    <div class="metric-label">Multicolinealidad</div>
                </div>
        """
        
        if multicollinearity:
            html += """
                <div class="alert alert-danger">
                    🚨 <strong>Alerta de Multicolinealidad:</strong> Se detectaron variables con correlación extrema (&gt; 0.95) que podrían causar problemas en el modelo.
                </div>
                
                <h3>Problemas de Multicolinealidad</h3>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Variable 1</th>
                            <th>Variable 2</th>
                            <th>Correlación</th>
                            <th>Problema</th>
                        </tr>
                    </thead>
                    <tbody>
            """
            
            for issue in multicollinearity[:10]:  # Máximo 10
                html += f"""
                    <tr style='background-color: #f8d7da;'>
                        <td>{issue['feature_1']}</td>
                        <td>{issue['feature_2']}</td>
                        <td>{issue['correlation']:.4f}</td>
                        <td>{issue['issue']}</td>
                    </tr>
                """
            
            html += "</tbody></table>"
        
        if high_corr and not multicollinearity:
            html += """
                <h3>Correlaciones Altas Detectadas</h3>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Variable 1</th>
                            <th>Variable 2</th>
                            <th>Correlación</th>
                        </tr>
                    </thead>
                    <tbody>
            """
            
            for corr in high_corr[:10]:  # Máximo 10
                html += f"""
                    <tr>
                        <td>{corr['feature_1']}</td>
                        <td>{corr['feature_2']}</td>
                        <td>{corr['correlation']:.4f}</td>
                    </tr>
                """
            
            html += "</tbody></table>"
        
        # Correlaciones con variables objetivo
        target_corr = correlations.get('target_correlations', {})
        if target_corr:
            html += "<h3>Correlaciones con Variables Objetivo</h3>"
            
            for target, corr_data in target_corr.items():
                top_positive = corr_data.get('top_positive', {})
                if top_positive:
                    html += f"""
                        <h4>Top correlaciones positivas con {target}:</h4>
                        <ul>
                    """
                    for feature, corr_val in list(top_positive.items())[:5]:
                        html += f"<li>{feature}: {corr_val:.4f}</li>"
                    html += "</ul>"
        
        html += "</div>"
        return html

    def _generate_distributions_section(self) -> str:
        """Genera sección de análisis de distribuciones."""
        distributions = self.report.get('detailed_analysis', {}).get('distributions', {})
        
        if not distributions:
            return ""
        
        summary = distributions.get('summary', {})
        
        html = f"""
            <div class="section">
                <h2 class="section-title">📈 Análisis de Distribuciones</h2>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('total_numeric_features', 0)}</div>
                    <div class="metric-label">Features Analizadas</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('normal_distributions', 0)}</div>
                    <div class="metric-label">Distribuciones Normales</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('skewed_distributions', 0)}</div>
                    <div class="metric-label">Distribuciones Asimétricas</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('percentage_normal', 0)}%</div>
                    <div class="metric-label">% Normales</div>
                </div>
        """
        
        distributions_by_col = distributions.get('distributions_by_column', {})
        if distributions_by_col:
            html += """
                <h3>Resumen de Distribuciones por Característica</h3>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Característica</th>
                            <th>Media</th>
                            <th>Desv. Estándar</th>
                            <th>Asimetría</th>
                            <th>Normalidad</th>
                            <th>Clasificación</th>
                        </tr>
                    </thead>
                    <tbody>
            """
            
            count = 0
            for col, dist_data in distributions_by_col.items():
                if count >= 15:  # Máximo 15 filas
                    break
                
                desc_stats = dist_data.get('descriptive_stats', {})
                normality = dist_data.get('normality_test', {})
                classification = dist_data.get('distribution_classification', 'No clasificada')
                
                is_normal = normality.get('is_normal', False)
                normal_class = ""
                if not is_normal:
                    normal_class = "style='background-color: #fff3cd;'"
                
                html += f"""
                    <tr {normal_class}>
                        <td>{col}</td>
                        <td>{desc_stats.get('mean', 0):.2f}</td>
                        <td>{desc_stats.get('std', 0):.2f}</td>
                        <td>{desc_stats.get('skewness', 0):.3f}</td>
                        <td>{'✅' if is_normal else '❌'}</td>
                        <td>{classification}</td>
                    </tr>
                """
                count += 1
            
            html += "</tbody></table>"
            
            if len(distributions_by_col) > 15:
                html += f"<p><em>Mostrando las primeras 15 características de {len(distributions_by_col)} analizadas.</em></p>"
        
        html += "</div>"
        return html

    def _generate_drift_section(self) -> str:
        """Genera sección de análisis de data drift."""
        drift = self.report.get('detailed_analysis', {}).get('drift', {})
        
        if not drift or not drift.get('summary', {}).get('temporal_column_found', False):
            return """
                <div class="section">
                    <h2 class="section-title">⏰ Análisis de Data Drift</h2>
                    <div class="alert alert-warning">
                        ⚠️ No se pudo realizar análisis de data drift temporal. No se encontró columna temporal válida.
                    </div>
                </div>
            """
        
        summary = drift.get('summary', {})
        drift_analysis = drift.get('drift_analysis', {})
        
        html = f"""
            <div class="section">
                <h2 class="section-title">⏰ Análisis de Data Drift</h2>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('features_analyzed', 0)}</div>
                    <div class="metric-label">Features Analizadas</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('features_with_drift', 0)}</div>
                    <div class="metric-label">Con Drift Detectado</div>
                </div>
                
                <div class="metric-card">
                    <div class="metric-value">{summary.get('percentage_drift', 0)}%</div>
                    <div class="metric-label">% Con Drift</div>
                </div>
                
                <p><strong>Columna Temporal:</strong> {drift.get('temporal_column', 'N/A')}</p>
                <p><strong>Períodos Comparados:</strong> {drift.get('comparison_periods', 0)}</p>
        """
        
        drift_features = summary.get('drift_features', [])
        if drift_features:
            html += """
                <div class="alert alert-warning">
                    ⚠️ <strong>Data Drift Detectado:</strong> Las siguientes características muestran cambios significativos a lo largo del tiempo.
                </div>
                
                <h3>Características con Drift Temporal</h3>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Característica</th>
                            <th>Coef. Variación</th>
                            <th>Cambio Relativo (%)</th>
                            <th>Rango de Medias</th>
                        </tr>
                    </thead>
                    <tbody>
            """
            
            for feature in drift_features[:10]:  # Máximo 10
                feature_data = drift_analysis.get(feature, {})
                drift_metrics = feature_data.get('drift_metrics', {})
                
                html += f"""
                    <tr style='background-color: #fff3cd;'>
                        <td>{feature}</td>
                        <td>{drift_metrics.get('coefficient_variation_means', 0):.4f}</td>
                        <td>{drift_metrics.get('relative_change_pct', 0):.2f}%</td>
                        <td>{drift_metrics.get('mean_range', 0):.2f}</td>
                    </tr>
                """
            
            html += "</tbody></table>"
        else:
            html += """
                <div class="alert alert-success">
                    ✅ No se detectó drift temporal significativo en las características analizadas.
                </div>
            """
        
        html += "</div>"
        return html

    def _generate_recommendations_section(self) -> str:
        """Genera sección de recomendaciones."""
        recommendations = self.report.get('recommendations', [])
        
        if not recommendations:
            return ""
        
        html = """
            <div class="section">
                <h2 class="section-title">💡 Recomendaciones</h2>
                <div class="recommendations">
        """
        
        for i, rec in enumerate(recommendations, 1):
            # Determinar tipo de recomendación basado en emoji/texto
            if '🚨' in rec or 'PRIORIDAD ALTA' in rec:
                alert_class = 'alert-danger'
            elif '⚠️' in rec or 'DETECTADO' in rec.upper():
                alert_class = 'alert-warning'
            else:
                alert_class = 'alert-success'
            
            html += f"""
                <div class="recommendation alert {alert_class}">
                    <strong>{i}.</strong> {rec}
                </div>
            """
        
        html += """
                </div>
            </div>
        """
        
        return html

    def _generate_html_footer(self) -> str:
        """Genera footer HTML."""
        timestamp = self.report.get('metadata', {}).get('timestamp', datetime.now().isoformat())
        
        return f"""
            <div class="timestamp">
                Reporte generado automáticamente el {timestamp}<br>
                Sistema POS & Gestión Integral - Analizador de Calidad de Datos v1.0.0
            </div>
        </div>
    </div>
</body>
</html>
"""


def generate_sample_html_report():
    """
    Función de demostración para generar un reporte HTML de ejemplo.
    """
    from data_quality_analyzer import DataQualityAnalyzer
    
    print("🎨 Generando reporte HTML de ejemplo...")
    
    # Generar datos de prueba
    df_test = DataQualityAnalyzer.generate_test_data(
        n_samples=1000, 
        n_features=20, 
        include_issues=True
    )
    
    # Crear analizador y generar reporte
    analyzer = DataQualityAnalyzer(df_test)
    quality_report = analyzer.generate_full_report()
    
    # Generar reporte HTML
    html_generator = HTMLReportGenerator(quality_report)
    html_content = html_generator.generate_html_report('ml-prediction-service/data_quality_report.html')
    
    print("✅ Reporte HTML generado exitosamente!")
    print("📁 Archivo: ml-prediction-service/data_quality_report.html")
    
    return html_content


if __name__ == "__main__":
    generate_sample_html_report()