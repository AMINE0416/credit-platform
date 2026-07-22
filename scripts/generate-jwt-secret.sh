#!/bin/bash
# Script pour générer un secret JWT sécurisé

echo "🔐 Génération d'un secret JWT sécurisé..."
echo ""
echo "Secret JWT (128 caractères hex) :"
openssl rand -hex 64
echo ""
echo "✅ Copiez ce secret et utilisez-le comme valeur pour JWT_SECRET"
echo "⚠️  Ne partagez JAMAIS ce secret publiquement"
