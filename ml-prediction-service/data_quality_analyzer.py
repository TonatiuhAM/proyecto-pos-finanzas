#!/usr/bin/env python3
"""
Analizador de Calidad de Datos para DataFrames Post-Feature Engineering
Sistema POS & Gestión Integral - Motor de Predicciones ML

Autor: Ingeniero de Datos Senior
Fecha: 14 de octubre de 2025
Versión: 1.0.0

Descripción:
    Script completo para análisis de calidad de datos de DataFrames que han pasado 
    por el proceso de feature engineering. Diseñado específicamente para el sistema
    POS con modelo XGBoost de predicción de compras.

Características:
    - Análisis de completitud y tipos de datos
    - Detección de outliers y análisis de distribuciones
    - Análisis de correlaciones y multicolinealidad
    - Detección de data drift temporal
    - Generación de reportes HTML interactivos
    - Función de generación de datos de prueba realistas
    - Métricas específicas para modelos de ML

Uso:
    python data_quality_analyzer.py
    
    O desde otro script:
    from data_quality_analyzer import DataQualityAnalyzer
    analyzer = DataQualityAnalyzer(df)
    report = analyzer.generate_full_report()
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from datetime import datetime, timedelta
import warnings
from typing import Dict, List, Tuple, Optional, Any
import json
from pathlib import Path

# Configuración de visualizaciones
plt.style.use('seaborn-v0_8')
sns.set_palette("husl")
warnings.filterwarnings('ignore')

class DataQualityAnalyzer:
    """
    Clase principal para análisis de calidad de datos post-feature engineering.
    
    Atributos:
        df (pd.DataFrame): DataFrame a analizar
        features_numericas (List[str]): Lista de características numéricas
        features_categoricas (List[str]): Lista de características categóricas
        features_temporales (List[str]): Lista de características temporales
        target_columns (List[str]): Columnas objetivo del modelo
        
    Métodos:
        - analyze_completeness(): Análisis de completitud de datos
        - analyze_data_types(): Análisis de tipos de datos
        - analyze_outliers(): Detección y análisis de outliers
        - analyze_distributions(): Análisis de distribuciones
        - analyze_correlations(): Análisis de correlaciones
        - detect_data_drift(): Detección de drift temporal
        - generate_test_data(): Generación de datos de prueba
        - generate_full_report(): Generación de reporte completo
    """
    
    def __init__(self, df: pd.DataFrame, target_columns: Optional[List[str]] = None):
        """
        Inicializa el analizador con el DataFrame.
        
        Args:
            df (pd.DataFrame): DataFrame post-feature engineering
            target_columns (List[str], optional): Columnas objetivo del modelo
        """
        self.df = df.copy()
        self.target_columns = target_columns or ['cantidad_a_comprar', 'prioridad_compra']
        
        # Clasificación automática de características
        self._classify_features()
        
        # Metadatos del análisis
        self.analysis_metadata = {
            'timestamp': datetime.now().isoformat(),
            'dataframe_shape': self.df.shape,
            'total_features': len(self.df.columns),
            'numeric_features': len(self.features_numericas),
            'categorical_features': len(self.features_categoricas),
            'temporal_features': len(self.features_temporales)
        }
        
        print(f"🔍 DataQualityAnalyzer inicializado:")
        print(f"   📊 Shape: {self.df.shape}")
        print(f"   📈 Features numéricas: {len(self.features_numericas)}")
        print(f"   📋 Features categóricas: {len(self.features_categoricas)}")
        print(f"   📅 Features temporales: {len(self.features_temporales)}")

    def _classify_features(self):
        """Clasifica automáticamente las características por tipo."""
        self.features_numericas = []
        self.features_categoricas = []
        self.features_temporales = []
        
        for col in self.df.columns:
            if self.df[col].dtype in ['int64', 'float64']:
                # Verificar si es categórica numérica (pocos valores únicos)
                unique_ratio = self.df[col].nunique() / len(self.df)
                if unique_ratio < 0.05 and self.df[col].nunique() < 20:
                    self.features_categoricas.append(col)
                else:
                    self.features_numericas.append(col)
            elif self.df[col].dtype == 'object':
                # Verificar si es temporal
                if any(keyword in col.lower() for keyword in ['fecha', 'date', 'time', 'timestamp']):
                    self.features_temporales.append(col)
                else:
                    self.features_categoricas.append(col)
            elif pd.api.types.is_datetime64_any_dtype(self.df[col]):
                self.features_temporales.append(col)
            else:
                self.features_categoricas.append(col)

    def analyze_completeness(self) -> Dict[str, Any]:
        """
        Analiza la completitud de los datos.
        
        Returns:
            Dict con métricas de completitud
        """
        print("📋 Analizando completitud de datos...")
        
        # Conteo de valores faltantes
        missing_counts = self.df.isnull().sum()
        missing_percentages = (missing_counts / len(self.df)) * 100
        
        completeness_summary = pd.DataFrame({
            'columna': self.df.columns,
            'valores_faltantes': missing_counts.values,
            'porcentaje_faltante': missing_percentages.values,
            'valores_completos': len(self.df) - missing_counts.values,
            'porcentaje_completo': 100 - missing_percentages.values
        }).sort_values('porcentaje_faltante', ascending=False)
        
        # Métricas de resumen
        total_cells = self.df.shape[0] * self.df.shape[1]
        total_missing = missing_counts.sum()
        overall_completeness = ((total_cells - total_missing) / total_cells) * 100
        
        # Columnas problemáticas (>5% faltantes)
        problematic_columns = completeness_summary[
            completeness_summary['porcentaje_faltante'] > 5
        ]['columna'].tolist()
        
        # Patrones de valores faltantes
        missing_patterns = self.df.isnull().sum(axis=1).value_counts().sort_index()
        
        return {
            'completeness_summary': completeness_summary,
            'overall_completeness_pct': round(overall_completeness, 2),
            'total_missing_values': int(total_missing),
            'problematic_columns': problematic_columns,
            'missing_patterns': missing_patterns.to_dict(),
            'columns_with_no_missing': completeness_summary[
                completeness_summary['porcentaje_faltante'] == 0
            ]['columna'].tolist()
        }

    def analyze_data_types(self) -> Dict[str, Any]:
        """
        Analiza los tipos de datos y su consistencia.
        
        Returns:
            Dict con análisis de tipos de datos
        """
        print("🔢 Analizando tipos de datos...")
        
        # Resumen de tipos
        types_summary = self.df.dtypes.value_counts()
        
        # Convertir los tipos a strings para serialización JSON
        types_dict = {}
        for dtype, count in types_summary.items():
            types_dict[str(dtype)] = int(count)
        
        # Análisis por tipo de feature
        type_analysis = {
            'dtype_distribution': types_dict,
            'feature_classification': {
                'numericas': self.features_numericas,
                'categoricas': self.features_categoricas,
                'temporales': self.features_temporales
            }
        }
        
        # Verificar inconsistencias de tipo
        inconsistencias = []
        
        # Verificar columnas numéricas que podrían ser categóricas
        for col in self.features_numericas:
            if col in self.df.columns:
                unique_ratio = self.df[col].nunique() / len(self.df)
                if unique_ratio < 0.01 and self.df[col].nunique() < 10:
                    inconsistencias.append({
                        'columna': col,
                        'problema': 'Numérica con pocos valores únicos - posible categórica',
                        'valores_unicos': self.df[col].nunique(),
                        'ratio_unicidad': round(unique_ratio, 4)
                    })
        
        # Verificar columnas object que podrían ser numéricas
        for col in self.features_categoricas:
            if col in self.df.columns and self.df[col].dtype == 'object':
                # Intentar conversión numérica
                try:
                    pd.to_numeric(self.df[col].dropna())
                    inconsistencias.append({
                        'columna': col,
                        'problema': 'Categórica que podría ser numérica',
                        'sample_values': self.df[col].dropna().head(5).tolist()
                    })
                except:
                    pass
        
        type_analysis['inconsistencias_detectadas'] = inconsistencias
        
        return type_analysis

    def analyze_outliers(self, method: str = 'iqr') -> Dict[str, Any]:
        """
        Detecta y analiza outliers en características numéricas.
        
        Args:
            method (str): Método de detección ('iqr', 'zscore', 'isolation')
            
        Returns:
            Dict con análisis de outliers
        """
        print(f"🎯 Detectando outliers usando método: {method}")
        
        outliers_analysis = {}
        
        for col in self.features_numericas:
            if col not in self.df.columns:
                continue
                
            col_data = self.df[col].dropna()
            if len(col_data) == 0:
                continue
            
            outliers_info = {
                'columna': col,
                'total_valores': len(col_data),
                'outliers_detectados': 0,
                'porcentaje_outliers': 0,
                'outliers_indices': [],
                'estadisticas': {}
            }
            
            if method == 'iqr':
                Q1 = col_data.quantile(0.25)
                Q3 = col_data.quantile(0.75)
                IQR = Q3 - Q1
                lower_bound = Q1 - 1.5 * IQR
                upper_bound = Q3 + 1.5 * IQR
                
                outliers_mask = (col_data < lower_bound) | (col_data > upper_bound)
                outliers_indices = col_data[outliers_mask].index.tolist()
                
                outliers_info.update({
                    'outliers_detectados': len(outliers_indices),
                    'porcentaje_outliers': round((len(outliers_indices) / len(col_data)) * 100, 2),
                    'outliers_indices': outliers_indices[:50],  # Primeros 50
                    'estadisticas': {
                        'Q1': Q1,
                        'Q3': Q3,
                        'IQR': IQR,
                        'lower_bound': lower_bound,
                        'upper_bound': upper_bound,
                        'min_outlier': col_data[outliers_mask].min() if len(outliers_indices) > 0 else None,
                        'max_outlier': col_data[outliers_mask].max() if len(outliers_indices) > 0 else None
                    }
                })
            
            elif method == 'zscore':
                z_scores = np.abs((col_data - col_data.mean()) / col_data.std())
                outliers_mask = z_scores > 3
                outliers_indices = col_data[outliers_mask].index.tolist()
                
                outliers_info.update({
                    'outliers_detectados': len(outliers_indices),
                    'porcentaje_outliers': round((len(outliers_indices) / len(col_data)) * 100, 2),
                    'outliers_indices': outliers_indices[:50],
                    'estadisticas': {
                        'mean': col_data.mean(),
                        'std': col_data.std(),
                        'max_zscore': z_scores.max(),
                        'outlier_threshold': 3
                    }
                })
            
            outliers_analysis[col] = outliers_info
        
        # Resumen general
        total_outliers = sum([info['outliers_detectados'] for info in outliers_analysis.values()])
        columns_with_outliers = [col for col, info in outliers_analysis.items() 
                               if info['outliers_detectados'] > 0]
        
        return {
            'method_used': method,
            'outliers_by_column': outliers_analysis,
            'summary': {
                'total_outliers_detected': total_outliers,
                'columns_with_outliers': columns_with_outliers,
                'percentage_columns_with_outliers': round(
                    (len(columns_with_outliers) / len(self.features_numericas)) * 100, 2
                ) if self.features_numericas else 0
            }
        }

    def analyze_distributions(self) -> Dict[str, Any]:
        """
        Analiza las distribuciones de características numéricas.
        
        Returns:
            Dict con análisis de distribuciones
        """
        print("📊 Analizando distribuciones de datos...")
        
        from scipy import stats
        
        distributions_analysis = {}
        
        for col in self.features_numericas:
            if col not in self.df.columns:
                continue
                
            col_data = self.df[col].dropna()
            if len(col_data) < 10:  # Datos insuficientes
                continue
            
            # Estadísticas descriptivas
            desc_stats = {
                'count': len(col_data),
                'mean': col_data.mean(),
                'median': col_data.median(),
                'std': col_data.std(),
                'min': col_data.min(),
                'max': col_data.max(),
                'skewness': stats.skew(col_data),
                'kurtosis': stats.kurtosis(col_data),
                'range': col_data.max() - col_data.min(),
                'coefficient_of_variation': col_data.std() / col_data.mean() if col_data.mean() != 0 else np.inf
            }
            
            # Percentiles
            percentiles = {
                'p1': col_data.quantile(0.01),
                'p5': col_data.quantile(0.05),
                'p25': col_data.quantile(0.25),
                'p75': col_data.quantile(0.75),
                'p95': col_data.quantile(0.95),
                'p99': col_data.quantile(0.99)
            }
            
            # Test de normalidad (Shapiro-Wilk para muestras pequeñas, Anderson-Darling para grandes)
            normality_test = {}
            if len(col_data) <= 5000:
                stat, p_value = stats.shapiro(col_data)
                normality_test = {
                    'test': 'Shapiro-Wilk',
                    'statistic': stat,
                    'p_value': p_value,
                    'is_normal': p_value > 0.05
                }
            else:
                # Para muestras grandes, usar muestra aleatoria
                sample_data = col_data.sample(n=5000, random_state=42)
                stat, p_value = stats.shapiro(sample_data)
                normality_test = {
                    'test': 'Shapiro-Wilk (muestra)',
                    'statistic': stat,
                    'p_value': p_value,
                    'is_normal': p_value > 0.05,
                    'note': 'Test realizado en muestra de 5000 observaciones'
                }
            
            # Clasificación de distribución
            distribution_type = self._classify_distribution(desc_stats)
            
            distributions_analysis[col] = {
                'descriptive_stats': desc_stats,
                'percentiles': percentiles,
                'normality_test': normality_test,
                'distribution_classification': distribution_type
            }
        
        return {
            'distributions_by_column': distributions_analysis,
            'summary': self._summarize_distributions(distributions_analysis)
        }

    def _classify_distribution(self, stats: Dict) -> str:
        """Clasifica el tipo de distribución basado en estadísticas."""
        skewness = abs(stats['skewness'])
        kurtosis = stats['kurtosis']
        cv = stats['coefficient_of_variation']
        
        if skewness < 0.5:
            if -0.5 < kurtosis < 0.5:
                return "Aproximadamente normal"
            elif kurtosis > 0.5:
                return "Leptocúrtica (colas pesadas)"
            else:
                return "Platicúrtica (colas ligeras)"
        elif skewness < 1:
            return "Moderadamente asimétrica"
        else:
            return "Altamente asimétrica"

    def _summarize_distributions(self, distributions: Dict) -> Dict:
        """Genera resumen de distribuciones."""
        if not distributions:
            return {}
        
        # Contadores
        normal_count = sum(1 for d in distributions.values() 
                          if d['normality_test']['is_normal'])
        skewed_count = sum(1 for d in distributions.values() 
                          if abs(d['descriptive_stats']['skewness']) > 1)
        high_variance_count = sum(1 for d in distributions.values() 
                                 if d['descriptive_stats']['coefficient_of_variation'] > 1)
        
        return {
            'total_numeric_features': len(distributions),
            'normal_distributions': normal_count,
            'skewed_distributions': skewed_count,
            'high_variance_features': high_variance_count,
            'percentage_normal': round((normal_count / len(distributions)) * 100, 2),
            'percentage_skewed': round((skewed_count / len(distributions)) * 100, 2)
        }

    def analyze_correlations(self, threshold: float = 0.8) -> Dict[str, Any]:
        """
        Analiza correlaciones entre características numéricas.
        
        Args:
            threshold (float): Umbral para correlaciones altas
            
        Returns:
            Dict con análisis de correlaciones
        """
        print(f"🔗 Analizando correlaciones (umbral: {threshold})")
        
        # Seleccionar solo características numéricas
        numeric_df = self.df[self.features_numericas].select_dtypes(include=[np.number])
        
        if numeric_df.shape[1] < 2:
            return {
                'correlation_matrix': {},
                'high_correlations': [],
                'multicollinearity_issues': [],
                'summary': {
                    'total_pairs': 0,
                    'high_correlation_pairs': 0
                }
            }
        
        # Matriz de correlación
        corr_matrix = numeric_df.corr()
        
        # Encontrar correlaciones altas
        high_correlations = []
        multicollinearity_issues = []
        
        for i in range(len(corr_matrix.columns)):
            for j in range(i+1, len(corr_matrix.columns)):
                col1 = corr_matrix.columns[i]
                col2 = corr_matrix.columns[j]
                corr_value = corr_matrix.iloc[i, j]
                
                if abs(corr_value) > threshold:
                    high_correlations.append({
                        'feature_1': col1,
                        'feature_2': col2,
                        'correlation': round(corr_value, 4),
                        'abs_correlation': round(abs(corr_value), 4)
                    })
                    
                    if abs(corr_value) > 0.95:
                        multicollinearity_issues.append({
                            'feature_1': col1,
                            'feature_2': col2,
                            'correlation': round(corr_value, 4),
                            'issue': 'Posible multicolinealidad extrema'
                        })
        
        # Ordenar por correlación absoluta
        high_correlations.sort(key=lambda x: x['abs_correlation'], reverse=True)
        
        # Análisis de correlaciones con variables objetivo
        target_correlations = {}
        for target in self.target_columns:
            if target in numeric_df.columns:
                target_corrs = corr_matrix[target].drop(target).sort_values(
                    key=abs, ascending=False
                )
                target_correlations[target] = {
                    'top_positive': target_corrs.head(5).to_dict(),
                    'top_negative': target_corrs.tail(5).to_dict(),
                    'strongest_overall': target_corrs.iloc[0] if len(target_corrs) > 0 else None
                }
        
        total_pairs = len(corr_matrix.columns) * (len(corr_matrix.columns) - 1) // 2
        
        return {
            'correlation_matrix': corr_matrix.round(4).to_dict(),
            'high_correlations': high_correlations,
            'multicollinearity_issues': multicollinearity_issues,
            'target_correlations': target_correlations,
            'summary': {
                'total_pairs': total_pairs,
                'high_correlation_pairs': len(high_correlations),
                'multicollinearity_pairs': len(multicollinearity_issues),
                'percentage_high_corr': round((len(high_correlations) / total_pairs) * 100, 2) if total_pairs > 0 else 0
            }
        }

    def detect_data_drift(self, temporal_column: str = None, 
                         comparison_periods: int = 4) -> Dict[str, Any]:
        """
        Detecta drift en los datos comparando períodos temporales.
        
        Args:
            temporal_column (str): Columna temporal para agrupar datos
            comparison_periods (int): Número de períodos a comparar
            
        Returns:
            Dict con análisis de data drift
        """
        print(f"⏰ Detectando data drift temporal...")
        
        # Si no se especifica columna temporal, intentar encontrar una
        if temporal_column is None:
            if self.features_temporales:
                temporal_column = self.features_temporales[0]
            else:
                return {
                    'drift_analysis': {},
                    'summary': {
                        'temporal_column_found': False,
                        'message': 'No se encontró columna temporal para análisis de drift'
                    }
                }
        
        if temporal_column not in self.df.columns:
            return {
                'drift_analysis': {},
                'summary': {
                    'temporal_column_found': False,
                    'message': f'Columna temporal {temporal_column} no encontrada'
                }
            }
        
        # Convertir a datetime si es necesario
        df_temp = self.df.copy()
        if not pd.api.types.is_datetime64_any_dtype(df_temp[temporal_column]):
            try:
                df_temp[temporal_column] = pd.to_datetime(df_temp[temporal_column])
            except:
                return {
                    'drift_analysis': {},
                    'summary': {
                        'temporal_column_found': False,
                        'message': f'No se pudo convertir {temporal_column} a formato datetime'
                    }
                }
        
        # Dividir datos en períodos
        df_temp = df_temp.sort_values(temporal_column)
        total_rows = len(df_temp)
        period_size = total_rows // comparison_periods
        
        if period_size < 10:
            return {
                'drift_analysis': {},
                'summary': {
                    'temporal_column_found': True,
                    'message': 'Datos insuficientes para análisis de drift temporal'
                }
            }
        
        # Crear períodos
        periods = []
        for i in range(comparison_periods):
            start_idx = i * period_size
            end_idx = (i + 1) * period_size if i < comparison_periods - 1 else total_rows
            period_data = df_temp.iloc[start_idx:end_idx]
            periods.append({
                'period': i + 1,
                'start_date': period_data[temporal_column].min(),
                'end_date': period_data[temporal_column].max(),
                'data': period_data
            })
        
        # Análisis de drift por característica numérica
        drift_analysis = {}
        
        for col in self.features_numericas:
            if col not in df_temp.columns:
                continue
            
            col_drift = {
                'feature': col,
                'periods_stats': [],
                'drift_detected': False,
                'drift_metrics': {}
            }
            
            # Estadísticas por período
            period_means = []
            period_stds = []
            
            for period in periods:
                period_data = period['data'][col].dropna()
                if len(period_data) > 0:
                    stats = {
                        'period': period['period'],
                        'start_date': period['start_date'].strftime('%Y-%m-%d'),
                        'end_date': period['end_date'].strftime('%Y-%m-%d'),
                        'count': len(period_data),
                        'mean': period_data.mean(),
                        'std': period_data.std(),
                        'median': period_data.median()
                    }
                    col_drift['periods_stats'].append(stats)
                    period_means.append(stats['mean'])
                    period_stds.append(stats['std'])
            
            # Calcular métricas de drift
            if len(period_means) >= 2:
                # Coeficiente de variación de las medias entre períodos
                mean_of_means = np.mean(period_means)
                std_of_means = np.std(period_means)
                cv_means = std_of_means / mean_of_means if mean_of_means != 0 else np.inf
                
                # Diferencia relativa entre primer y último período
                relative_change = ((period_means[-1] - period_means[0]) / 
                                 period_means[0] * 100) if period_means[0] != 0 else np.inf
                
                col_drift['drift_metrics'] = {
                    'coefficient_variation_means': round(cv_means, 4),
                    'relative_change_pct': round(relative_change, 2),
                    'max_mean': max(period_means),
                    'min_mean': min(period_means),
                    'mean_range': max(period_means) - min(period_means)
                }
                
                # Detectar drift (umbral configurable)
                col_drift['drift_detected'] = cv_means > 0.1 or abs(relative_change) > 20
            
            drift_analysis[col] = col_drift
        
        # Resumen general
        features_with_drift = [col for col, analysis in drift_analysis.items() 
                              if analysis['drift_detected']]
        
        return {
            'temporal_column': temporal_column,
            'comparison_periods': comparison_periods,
            'drift_analysis': drift_analysis,
            'summary': {
                'temporal_column_found': True,
                'features_analyzed': len(drift_analysis),
                'features_with_drift': len(features_with_drift),
                'drift_features': features_with_drift,
                'percentage_drift': round((len(features_with_drift) / len(drift_analysis)) * 100, 2) if drift_analysis else 0
            }
        }

    def generate_full_report(self, output_file: str = None) -> Dict[str, Any]:
        """
        Genera reporte completo de calidad de datos.
        
        Args:
            output_file (str): Ruta para guardar reporte JSON
            
        Returns:
            Dict con reporte completo
        """
        print("📋 Generando reporte completo de calidad de datos...")
        
        # Ejecutar todos los análisis
        completeness = self.analyze_completeness()
        data_types = self.analyze_data_types()
        outliers = self.analyze_outliers()
        distributions = self.analyze_distributions()
        correlations = self.analyze_correlations()
        drift = self.detect_data_drift()
        
        # Consolidar reporte
        full_report = {
            'metadata': self.analysis_metadata,
            'executive_summary': self._generate_executive_summary(
                completeness, data_types, outliers, distributions, correlations, drift
            ),
            'detailed_analysis': {
                'completeness': completeness,
                'data_types': data_types,
                'outliers': outliers,
                'distributions': distributions,
                'correlations': correlations,
                'drift': drift
            },
            'recommendations': self._generate_recommendations(
                completeness, data_types, outliers, distributions, correlations, drift
            )
        }
        
        # Guardar reporte si se especifica archivo
        if output_file:
            output_path = Path(output_file)
            output_path.parent.mkdir(parents=True, exist_ok=True)
            
            # Función helper para convertir tipos no serializables
            def convert_for_json(obj):
                if hasattr(obj, 'dtype'):
                    return str(obj)
                elif isinstance(obj, np.ndarray):
                    return obj.tolist()
                elif isinstance(obj, (np.int64, np.int32, np.float64, np.float32)):
                    return obj.item()
                elif hasattr(obj, '__dict__'):
                    return str(obj)
                else:
                    return str(obj)
            
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(full_report, f, indent=2, ensure_ascii=False, default=convert_for_json)
            
            print(f"💾 Reporte guardado en: {output_path}")
        
        return full_report

    def _generate_executive_summary(self, completeness, data_types, outliers, 
                                  distributions, correlations, drift) -> Dict[str, Any]:
        """Genera resumen ejecutivo del análisis."""
        return {
            'data_quality_score': self._calculate_quality_score(
                completeness, data_types, outliers, correlations
            ),
            'key_findings': {
                'completeness': f"{completeness['overall_completeness_pct']}% de completitud general",
                'outliers': f"{outliers['summary']['total_outliers_detected']} outliers detectados",
                'correlations': f"{correlations['summary']['high_correlation_pairs']} pares de alta correlación",
                'drift': f"{drift['summary'].get('features_with_drift', 0)} características con drift" if drift['summary'].get('temporal_column_found') else "No se pudo analizar drift temporal"
            },
            'quality_flags': {
                'high_missing_data': completeness['overall_completeness_pct'] < 95,
                'excessive_outliers': outliers['summary']['total_outliers_detected'] > len(self.df) * 0.05,
                'multicollinearity_issues': len(correlations['multicollinearity_issues']) > 0,
                'data_drift_detected': drift['summary'].get('features_with_drift', 0) > 0
            }
        }

    def _calculate_quality_score(self, completeness, data_types, outliers, correlations) -> float:
        """Calcula puntuación de calidad de datos (0-100)."""
        score = 100
        
        # Penalizar por completitud
        score -= (100 - completeness['overall_completeness_pct']) * 0.5
        
        # Penalizar por outliers excesivos
        outlier_ratio = outliers['summary']['total_outliers_detected'] / len(self.df)
        if outlier_ratio > 0.05:
            score -= (outlier_ratio - 0.05) * 100
        
        # Penalizar por problemas de multicolinealidad
        score -= len(correlations['multicollinearity_issues']) * 5
        
        # Penalizar por inconsistencias de tipo
        score -= len(data_types['inconsistencias_detectadas']) * 3
        
        return max(0, round(score, 2))

    def _generate_recommendations(self, completeness, data_types, outliers, 
                                distributions, correlations, drift) -> List[str]:
        """Genera recomendaciones basadas en el análisis."""
        recommendations = []
        
        # Recomendaciones de completitud
        if completeness['overall_completeness_pct'] < 95:
            recommendations.append(
                f"🚨 PRIORIDAD ALTA: Completitud de datos ({completeness['overall_completeness_pct']}%) "
                f"está por debajo del umbral recomendado (95%). "
                f"Revisar columnas: {', '.join(completeness['problematic_columns'][:3])}"
            )
        
        # Recomendaciones de outliers
        if outliers['summary']['total_outliers_detected'] > 0:
            recommendations.append(
                f"🔍 Revisar {outliers['summary']['total_outliers_detected']} outliers detectados. "
                f"Verificar si son errores de datos o valores legítimos."
            )
        
        # Recomendaciones de correlaciones
        if correlations['multicollinearity_issues']:
            recommendations.append(
                f"⚠️ Detectada multicolinealidad en {len(correlations['multicollinearity_issues'])} pares de variables. "
                f"Considerar eliminación de variables redundantes o técnicas de reducción de dimensionalidad."
            )
        
        # Recomendaciones de distribuciones
        if distributions['summary'].get('percentage_skewed', 0) > 50:
            recommendations.append(
                f"📊 {distributions['summary']['percentage_skewed']}% de variables numéricas presentan asimetría. "
                f"Considerar transformaciones (log, sqrt, Box-Cox) para normalización."
            )
        
        # Recomendaciones de drift
        if drift['summary'].get('features_with_drift', 0) > 0:
            recommendations.append(
                f"⏰ Detectado drift temporal en {drift['summary']['features_with_drift']} características. "
                f"Revisar estabilidad del modelo y considerar reentrenamiento."
            )
        
        # Recomendaciones de tipos
        if data_types['inconsistencias_detectadas']:
            recommendations.append(
                f"🔧 Detectadas {len(data_types['inconsistencias_detectadas'])} inconsistencias de tipo de datos. "
                f"Revisar y corregir clasificación de variables."
            )
        
        if not recommendations:
            recommendations.append("✅ Los datos presentan buena calidad general. Mantener procesos de monitoreo actuales.")
        
        return recommendations

    @staticmethod
    def generate_test_data(n_samples: int = 1000, n_features: int = 30, 
                          include_issues: bool = True) -> pd.DataFrame:
        """
        Genera datos de prueba simulando el DataFrame post-feature engineering del sistema POS.
        
        Args:
            n_samples (int): Número de muestras
            n_features (int): Número de características
            include_issues (bool): Si incluir problemas de calidad intencionalmente
            
        Returns:
            pd.DataFrame: DataFrame de prueba
        """
        print(f"🧪 Generando datos de prueba: {n_samples} muestras, {n_features} características")
        
        np.random.seed(42)  # Para reproducibilidad
        
        # Definir características basadas en el sistema real
        base_features = [
            # Características del producto
            'precio_producto', 'costo_producto', 'margen_ganancia',
            'stock_actual', 'stock_minimo', 'rotacion_inventario',
            
            # Características temporales y de estacionalidad
            'dias_desde_ultima_venta', 'ventas_ultimos_7_dias', 'ventas_ultimos_30_dias',
            'tendencia_ventas', 'estacionalidad_mes', 'estacionalidad_dia_semana',
            
            # Características de proveedor
            'confiabilidad_proveedor', 'tiempo_entrega_promedio', 'calidad_historial',
            'descuento_volumen', 'precio_proveedor_normalizado',
            
            # Características de demanda
            'demanda_predicha', 'volatilidad_demanda', 'patron_estacional',
            'elasticidad_precio', 'correlacion_productos_relacionados',
            
            # Características económicas y de contexto
            'inflacion_categoria', 'competencia_precio', 'indice_popularidad',
            'categoria_producto_encoded', 'subcategoria_encoded',
            
            # Variables objetivo
            'cantidad_a_comprar', 'prioridad_compra'
        ]
        
        # Seleccionar características basadas en n_features
        if n_features <= len(base_features):
            selected_features = base_features[:n_features]
        else:
            # Añadir características adicionales genéricas
            additional_features = [f'feature_{i}' for i in range(len(base_features), n_features)]
            selected_features = base_features + additional_features
        
        # Generar datos por tipo de característica
        data = {}
        
        for i, feature in enumerate(selected_features):
            if 'precio' in feature or 'costo' in feature:
                # Precios con distribución log-normal
                data[feature] = np.random.lognormal(mean=3, sigma=1, size=n_samples)
                
            elif 'stock' in feature or 'cantidad' in feature:
                # Cantidades enteras positivas
                data[feature] = np.random.poisson(lam=50, size=n_samples)
                
            elif 'dias' in feature or 'tiempo' in feature:
                # Días/tiempo con distribución exponencial
                data[feature] = np.random.exponential(scale=10, size=n_samples)
                
            elif 'porcentaje' in feature or 'ratio' in feature or 'margen' in feature:
                # Porcentajes/ratios entre 0 y 1
                data[feature] = np.random.beta(a=2, b=5, size=n_samples)
                
            elif 'encoded' in feature or 'categoria' in feature:
                # Variables categóricas codificadas
                data[feature] = np.random.randint(0, 10, size=n_samples)
                
            elif 'prioridad' in feature:
                # Prioridad discreta (1-5)
                data[feature] = np.random.choice([1, 2, 3, 4, 5], size=n_samples, 
                                               p=[0.1, 0.2, 0.4, 0.2, 0.1])
                
            else:
                # Características numéricas generales con distribución normal
                data[feature] = np.random.normal(loc=100, scale=20, size=n_samples)
        
        df = pd.DataFrame(data)
        
        # Introducir problemas de calidad intencionalmente si se solicita
        if include_issues:
            print("🎭 Introduciendo problemas de calidad intencionalmente...")
            
            # 1. Valores faltantes (5-10% en algunas columnas)
            missing_columns = np.random.choice(selected_features, 
                                             size=max(1, len(selected_features)//4), 
                                             replace=False)
            for col in missing_columns:
                missing_idx = np.random.choice(df.index, 
                                             size=int(len(df) * np.random.uniform(0.05, 0.10)), 
                                             replace=False)
                df.loc[missing_idx, col] = np.nan
            
            # 2. Outliers extremos (1-2% de datos)
            outlier_columns = [col for col in selected_features if col not in ['categoria_producto_encoded', 'subcategoria_encoded', 'prioridad_compra']]
            for col in outlier_columns[:3]:  # Solo en algunas columnas
                outlier_idx = np.random.choice(df.index, 
                                             size=int(len(df) * 0.02), 
                                             replace=False)
                # Multiplicar por factor aleatorio grande
                df.loc[outlier_idx, col] = df.loc[outlier_idx, col] * np.random.uniform(10, 50, size=len(outlier_idx))
            
            # 3. Correlación artificial alta entre algunas variables
            if len(selected_features) >= 4:
                # Hacer que feature_2 sea altamente correlacionada con feature_1
                col1, col2 = selected_features[0], selected_features[1]
                noise = np.random.normal(0, df[col1].std() * 0.1, size=len(df))
                df[col2] = df[col1] * 0.95 + noise
            
            # 4. Introducir drift temporal si hay suficientes datos
            if n_samples >= 500:
                # Simular drift en la segunda mitad de los datos
                drift_start = n_samples // 2
                drift_columns = selected_features[:2]
                for col in drift_columns:
                    # Incrementar valores gradualmente
                    drift_factor = np.linspace(1, 1.5, n_samples - drift_start)
                    df.loc[drift_start:, col] = df.loc[drift_start:, col] * drift_factor
        
        # Añadir columna temporal para análisis de drift
        start_date = datetime(2024, 1, 1)
        date_range = pd.date_range(start=start_date, periods=n_samples, freq='H')
        df['timestamp'] = date_range
        
        print(f"✅ Datos de prueba generados exitosamente")
        print(f"   📊 Shape: {df.shape}")
        print(f"   🔧 Problemas incluidos: {'Sí' if include_issues else 'No'}")
        
        return df


def main():
    """
    Función principal para demostración del analizador.
    """
    print("🚀 Demo: Analizador de Calidad de Datos - Sistema POS")
    print("=" * 60)
    
    # Generar datos de prueba
    print("\n1️⃣ Generando datos de prueba...")
    df_test = DataQualityAnalyzer.generate_test_data(
        n_samples=2000, 
        n_features=25, 
        include_issues=True
    )
    
    # Crear analizador
    print("\n2️⃣ Inicializando analizador...")
    analyzer = DataQualityAnalyzer(
        df=df_test,
        target_columns=['cantidad_a_comprar', 'prioridad_compra']
    )
    
    # Generar reporte completo
    print("\n3️⃣ Generando reporte completo...")
    report = analyzer.generate_full_report(
        output_file='ml-prediction-service/data_quality_report.json'
    )
    
    # Mostrar resumen ejecutivo
    print("\n" + "="*60)
    print("📋 RESUMEN EJECUTIVO")
    print("="*60)
    
    summary = report['executive_summary']
    print(f"🎯 Puntuación de Calidad: {summary['data_quality_score']}/100")
    
    print("\n🔍 Hallazgos Clave:")
    for key, finding in summary['key_findings'].items():
        print(f"   • {finding}")
    
    print("\n⚠️ Flags de Calidad:")
    for flag, status in summary['quality_flags'].items():
        status_emoji = "❌" if status else "✅"
        print(f"   {status_emoji} {flag.replace('_', ' ').title()}: {status}")
    
    print("\n💡 Recomendaciones:")
    for i, rec in enumerate(report['recommendations'], 1):
        print(f"   {i}. {rec}")
    
    print(f"\n📁 Reporte completo guardado en: ml-prediction-service/data_quality_report.json")
    print("\n🎉 Análisis completado exitosamente!")


if __name__ == "__main__":
    main()