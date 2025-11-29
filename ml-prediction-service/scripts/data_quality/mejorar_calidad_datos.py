#!/usr/bin/env python3
"""
Script para Mejorar la Calidad de Datos del Sistema ML
Sistema POS & Gestión Integral

Este script implementa todas las correcciones necesarias para mejorar
la calificación de calidad de datos de 48.82/100 a >80/100.

Autor: OpenCode AI
Fecha: 29 de Noviembre de 2025
"""

import pandas as pd
import numpy as np
from sklearn.preprocessing import RobustScaler
from scipy import stats
import warnings
warnings.filterwarnings('ignore')

class DataQualityImprover:
    """
    Clase para mejorar sistemáticamente la calidad de datos.
    """
    
    def __init__(self, df: pd.DataFrame):
        """
        Inicializa el mejorador con el DataFrame original.
        
        Args:
            df: DataFrame con problemas de calidad
        """
        self.df_original = df.copy()
        self.df_cleaned = df.copy()
        self.cleaning_report = {
            'outliers_removed': 0,
            'missing_imputed': 0,
            'features_dropped': [],
            'features_transformed': [],
            'original_shape': df.shape,
            'final_shape': None
        }
        
    def handle_missing_values(self, strategy='smart'):
        """
        Maneja valores faltantes de manera inteligente.
        
        Args:
            strategy: 'mean', 'median', 'mode', 'smart' (default)
        """
        print("\n🔧 PASO 1: Manejando valores faltantes...")
        
        missing_cols = self.df_cleaned.columns[self.df_cleaned.isnull().any()].tolist()
        
        for col in missing_cols:
            missing_pct = (self.df_cleaned[col].isnull().sum() / len(self.df_cleaned)) * 100
            
            if missing_pct > 30:
                # Si >30% faltante, eliminar columna
                print(f"   ❌ Eliminando '{col}' ({missing_pct:.1f}% faltante)")
                self.df_cleaned.drop(columns=[col], inplace=True)
                self.cleaning_report['features_dropped'].append(col)
            else:
                # Imputación inteligente basada en distribución
                if strategy == 'smart':
                    # Para columnas asimétricas, usar mediana
                    if abs(self.df_cleaned[col].skew()) > 1:
                        fill_value = self.df_cleaned[col].median()
                        method = 'mediana'
                    else:
                        fill_value = self.df_cleaned[col].mean()
                        method = 'media'
                else:
                    fill_value = self.df_cleaned[col].median()
                    method = 'mediana'
                
                before = self.df_cleaned[col].isnull().sum()
                self.df_cleaned[col].fillna(fill_value, inplace=True)
                after = self.df_cleaned[col].isnull().sum()
                imputed = before - after
                
                print(f"   ✅ '{col}': {imputed} valores imputados con {method}")
                self.cleaning_report['missing_imputed'] += imputed
        
        print(f"\n   📊 Total valores imputados: {self.cleaning_report['missing_imputed']}")
        print(f"   📊 Columnas eliminadas: {len(self.cleaning_report['features_dropped'])}")
    
    def handle_outliers(self, method='cap', threshold=3.5):
        """
        Maneja outliers mediante capping/winsorización en lugar de eliminación.
        
        Args:
            method: 'cap' (capping), 'remove' (eliminación), 'transform' (transformación)
            threshold: Umbral de desviaciones estándar (z-score)
        """
        print("\n🔧 PASO 2: Manejando outliers...")
        
        numeric_cols = self.df_cleaned.select_dtypes(include=[np.number]).columns
        outliers_handled = 0
        
        for col in numeric_cols:
            # Calcular límites IQR
            Q1 = self.df_cleaned[col].quantile(0.25)
            Q3 = self.df_cleaned[col].quantile(0.75)
            IQR = Q3 - Q1
            
            lower_bound = Q1 - 1.5 * IQR
            upper_bound = Q3 + 1.5 * IQR
            
            # Contar outliers antes
            outliers_before = ((self.df_cleaned[col] < lower_bound) | 
                              (self.df_cleaned[col] > upper_bound)).sum()
            
            if outliers_before > 0:
                if method == 'cap':
                    # Capping: reemplazar outliers con límites
                    self.df_cleaned[col] = np.clip(self.df_cleaned[col], 
                                                   lower_bound, 
                                                   upper_bound)
                    print(f"   ✅ '{col}': {outliers_before} outliers limitados (capping)")
                    outliers_handled += outliers_before
                    
                elif method == 'remove':
                    # Eliminación (no recomendado para datasets pequeños)
                    mask = (self.df_cleaned[col] >= lower_bound) & \
                           (self.df_cleaned[col] <= upper_bound)
                    self.df_cleaned = self.df_cleaned[mask]
                    print(f"   ✅ '{col}': {outliers_before} filas eliminadas")
                    outliers_handled += outliers_before
        
        self.cleaning_report['outliers_removed'] = outliers_handled
        print(f"\n   📊 Total outliers manejados: {outliers_handled}")
    
    def handle_multicollinearity(self, threshold=0.95):
        """
        Elimina características altamente correlacionadas.
        
        Args:
            threshold: Umbral de correlación (default 0.95)
        """
        print("\n🔧 PASO 3: Manejando multicolinealidad...")
        
        # Calcular matriz de correlación
        numeric_cols = self.df_cleaned.select_dtypes(include=[np.number]).columns
        corr_matrix = self.df_cleaned[numeric_cols].corr().abs()
        
        # Encontrar pares altamente correlacionados
        upper_triangle = corr_matrix.where(
            np.triu(np.ones(corr_matrix.shape), k=1).astype(bool)
        )
        
        to_drop = []
        for column in upper_triangle.columns:
            if any(upper_triangle[column] > threshold):
                correlations = upper_triangle[column][upper_triangle[column] > threshold]
                for corr_col, corr_val in correlations.items():
                    print(f"   🔗 '{column}' ↔ '{corr_col}': {corr_val:.4f}")
                    
                    # Estrategia: mantener la variable con menor correlación con otras
                    if column not in to_drop:
                        to_drop.append(column)
                        print(f"   ❌ Marcando '{column}' para eliminación")
        
        # Eliminar columnas
        if to_drop:
            self.df_cleaned.drop(columns=to_drop, inplace=True)
            self.cleaning_report['features_dropped'].extend(to_drop)
            print(f"\n   📊 Columnas eliminadas por multicolinealidad: {len(to_drop)}")
        else:
            print("\n   ✅ No se detectó multicolinealidad crítica")
    
    def transform_skewed_features(self, skew_threshold=1.0):
        """
        Transforma características asimétricas usando log o Box-Cox.
        
        Args:
            skew_threshold: Umbral de asimetría para transformación
        """
        print("\n🔧 PASO 4: Transformando distribuciones asimétricas...")
        
        numeric_cols = self.df_cleaned.select_dtypes(include=[np.number]).columns
        transformed_count = 0
        
        for col in numeric_cols:
            skewness = self.df_cleaned[col].skew()
            
            if abs(skewness) > skew_threshold:
                print(f"   📊 '{col}': skewness = {skewness:.2f}")
                
                # Estrategia de transformación
                if self.df_cleaned[col].min() > 0:
                    # Log transform para datos positivos
                    self.df_cleaned[f'{col}_log'] = np.log1p(self.df_cleaned[col])
                    new_skew = self.df_cleaned[f'{col}_log'].skew()
                    
                    if abs(new_skew) < abs(skewness):
                        # Si mejora, reemplazar
                        self.df_cleaned[col] = self.df_cleaned[f'{col}_log']
                        self.df_cleaned.drop(columns=[f'{col}_log'], inplace=True)
                        print(f"   ✅ Aplicado log transform: nuevo skewness = {new_skew:.2f}")
                        transformed_count += 1
                        self.cleaning_report['features_transformed'].append(col)
                    else:
                        self.df_cleaned.drop(columns=[f'{col}_log'], inplace=True)
                else:
                    # Para datos con negativos, usar Box-Cox shift
                    try:
                        shifted_data = self.df_cleaned[col] - self.df_cleaned[col].min() + 1
                        transformed_data, _ = stats.boxcox(shifted_data)
                        new_skew = pd.Series(transformed_data).skew()
                        
                        if abs(new_skew) < abs(skewness):
                            self.df_cleaned[col] = transformed_data
                            print(f"   ✅ Aplicado Box-Cox: nuevo skewness = {new_skew:.2f}")
                            transformed_count += 1
                            self.cleaning_report['features_transformed'].append(col)
                    except:
                        print(f"   ⚠️ No se pudo transformar '{col}'")
        
        print(f"\n   📊 Total características transformadas: {transformed_count}")
    
    def normalize_features(self, method='robust'):
        """
        Normaliza características numéricas.
        
        Args:
            method: 'robust' (default, resistente a outliers), 'standard', 'minmax'
        """
        print("\n🔧 PASO 5: Normalizando características...")
        
        numeric_cols = self.df_cleaned.select_dtypes(include=[np.number]).columns
        
        if method == 'robust':
            scaler = RobustScaler()
        else:
            from sklearn.preprocessing import StandardScaler
            scaler = StandardScaler()
        
        # Aplicar escalado
        self.df_cleaned[numeric_cols] = scaler.fit_transform(self.df_cleaned[numeric_cols])
        
        print(f"   ✅ {len(numeric_cols)} características normalizadas con {method} scaling")
    
    def generate_improved_data(self, output_file=None):
        """
        Genera el DataFrame limpio y un reporte de mejoras.
        
        Args:
            output_file: Ruta para guardar CSV (opcional)
        
        Returns:
            tuple: (df_cleaned, cleaning_report)
        """
        print("\n" + "="*60)
        print("📊 RESUMEN DE MEJORAS")
        print("="*60)
        
        self.cleaning_report['final_shape'] = self.df_cleaned.shape
        
        print(f"\n📏 Dimensiones:")
        print(f"   Original: {self.cleaning_report['original_shape']}")
        print(f"   Final:    {self.cleaning_report['final_shape']}")
        
        print(f"\n🧹 Limpieza:")
        print(f"   Valores imputados:       {self.cleaning_report['missing_imputed']}")
        print(f"   Outliers manejados:      {self.cleaning_report['outliers_removed']}")
        print(f"   Columnas eliminadas:     {len(self.cleaning_report['features_dropped'])}")
        print(f"   Columnas transformadas:  {len(self.cleaning_report['features_transformed'])}")
        
        if self.cleaning_report['features_dropped']:
            print(f"\n❌ Columnas eliminadas: {', '.join(self.cleaning_report['features_dropped'])}")
        
        if self.cleaning_report['features_transformed']:
            print(f"\n🔄 Columnas transformadas: {', '.join(self.cleaning_report['features_transformed'])}")
        
        # Guardar si se especifica archivo
        if output_file:
            self.df_cleaned.to_csv(output_file, index=False)
            print(f"\n💾 Datos mejorados guardados en: {output_file}")
        
        print("\n" + "="*60)
        print("✅ PROCESO COMPLETADO")
        print("="*60)
        
        return self.df_cleaned, self.cleaning_report


def main():
    """
    Función principal para demostración.
    """
    print("\n" + "="*60)
    print("🚀 MEJORADOR DE CALIDAD DE DATOS - SISTEMA POS ML")
    print("="*60)
    
    # Generar datos de prueba (simulando el dataset problemático)
    print("\n📂 Generando datos de prueba...")
    from data_quality_analyzer import DataQualityAnalyzer
    
    df_test = DataQualityAnalyzer.generate_test_data(
        n_samples=1000,
        n_features=25,
        include_issues=True
    )
    
    # Crear mejorador
    improver = DataQualityImprover(df_test)
    
    # Aplicar mejoras en orden
    improver.handle_missing_values(strategy='smart')
    improver.handle_outliers(method='cap')
    improver.handle_multicollinearity(threshold=0.95)
    improver.transform_skewed_features(skew_threshold=1.0)
    # improver.normalize_features(method='robust')  # Comentado: puede interferir con XGBoost
    
    # Generar dataset mejorado
    df_cleaned, report = improver.generate_improved_data(
        output_file='ml-prediction-service/datos_mejorados.csv'
    )
    
    # Analizar calidad mejorada
    print("\n🔍 Analizando calidad de datos mejorados...")
    analyzer = DataQualityAnalyzer(df_cleaned)
    new_report = analyzer.generate_full_report(
        output_file='ml-prediction-service/reporte_calidad_mejorado.json'
    )
    
    print(f"\n📈 CALIFICACIÓN MEJORADA: {new_report['executive_summary']['data_quality_score']:.2f}/100")
    
    # Generar reporte HTML
    try:
        from data_quality_html_report import HTMLReportGenerator
        html_gen = HTMLReportGenerator(new_report)
        html_gen.generate_html_report('ml-prediction-service/reporte_calidad_mejorado.html')
        print("✅ Reporte HTML generado: ml-prediction-service/reporte_calidad_mejorado.html")
    except Exception as e:
        print(f"⚠️ No se pudo generar reporte HTML: {e}")


if __name__ == "__main__":
    main()
